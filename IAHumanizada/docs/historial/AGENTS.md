# AI Agent Guidelines for IAHumanizada

## Architecture Overview
This is an Android Compose app implementing a personality-driven AI chat system with context-aware responses. The system uses a "soul" metaphor: each personality (alma) evolves through reinforced learnings that get promoted to a persistent soul.

**Core Components:**
- **PersonalityEngine** (`engine/`): Orchestrates message processing, blends parameters, generates prompts
- **StateDetector** (`engine/`): Detects user emotional/contextual state from message content and pause duration
- **MemoryManager** (`engine/`): Manages three tiers: declarative memories (recent context), learnings (reinforced patterns), soul (DataStore-persisted learnings)
- **AlmaRepository** (`data/repository/`): Loads 6 JSON personalities from `app/src/main/res/raw/`
- **ApiService** (`data/repository/`): HTTP client for Gemini API (uses native `HttpURLConnection`, no Retrofit)
- **Room Database**: Stores Memory and Learning entities with hit counts and timestamps for selective forgetting

Data flow: User message → StateDetector (state inference) → PersonalityEngine (parameter blending) → MemoryManager (context building) → ApiService (HTTP to Gemini) → Response

## Key Patterns

### Alma Structure & Loading
- **Definition**: 6 JSON files in `raw/` directory (alma_poetica, alma_tecnica, alma_intima, alma_ironica, alma_sensual, alma_neutra)
- **Fields**: `id` (string), `nombre`, `descripcion`, `parametros` (Map<String, Float>), `vocabulario` (List), `microgestos`, `ejemplos`
- **Parameter Naming**: Almas use descriptive keys like `tono_calido` or `ritmo_lento`. `PersonalityEngine.calcularParametros()` uses prefix matching (`getParam("tono")` finds any key starting with "tono")
- **Loading**: `AlmaRepository` uses GSON with caching to avoid repeated file I/O

### Parameter Calculation (Blending)
Three inputs blended in `calcularParametros()`:
1. **Base Alma**: Loaded JSON parameters (70-60% weight)
2. **Afinacion** (User Tuning): 6 sliders (tono, ritmo, humor, microgestos, profundidad, presencia) from Altar UI (30-40% weight)
3. **Estado Modifiers**: State-based adjustments applied after blending (e.g., CALIDO adds +0.3 tono, TECNICO adds +0.4 tono)

All final parameters clamped to [0, 1]. Examples:
- `tono = (alma.tono * 0.7 + afinacion.tono * 0.3).coerceIn(0f, 1f)`
- `ritmo = (alma.ritmo * 0.6 + afinacion.ritmo * 0.4).coerceIn(0f, 1f)`

### State Detection (EstadoTipo Enum)
Detected via 7 sensors in `StateDetector.detectar()`:
1. **Pause Duration**: >300s + short message → SILENCIOSO; >60s → +1 CALIDO/REFLEXIVO
2. **Message Length**: Empty → SILENCIOSO; >20 words → REFLEXIVO
3. **Emotional Keywords**: "hola", "gracias", "cansado", "agobiado" → CALIDO (up to +5 points)
4. **Technical Keywords**: "código", "bug", "api", "gradle" → TECNICO (+5)
5. **Humor**: "jaja", "xd", "😂" → JUGUETON (+4)
6. **Reflection**: "por qué", "sentido", "pienso" → REFLEXIVO (+4)
7. **Punctuation**: "..." → REFLEXIVO (+2); multiple "!" → JUGUETON (+2)

Result: Enum returns highest-scoring state, defaults to NEUTRO if all are 0.

### Prompt Generation
Fixed structure with 7 sections:
```
[IDENTIDAD] - Alma description + vocabulary list
[PARÁMETROS ACTIVOS] - Numeric values (tono, ritmo, humor, profundidad, presencia)
[ESTADO ACTUAL] - User state + energy adaptation instruction
[ESTILO] - Microgestures list + state-specific rules (SILENCIOSO → minimal phrases)
[CONTEXTO CONVERSACIONAL] - Memory context (soul + recent + top learnings)
[MENSAJE DEL USUARIO] - Raw user input
[RESPUESTA] - Empty or prefixed with random microgesture if microgestos param > 0.5
```
Prompt sent to Gemini with temperature=0.6, maxTokens=2048.

### Memory System (3 Tiers)
1. **Declarative Memories** (Room, short-term):
   - Stored via `memoryManager.store(key, content)`
   - Tracked with hits counter (incremented on re-access)
   - Retrieved as recent context (last 5 by default)

2. **Learnings** (Room, reinforced patterns):
   - Created via `memoryManager.learn(key, content, category, archetypeId)`
   - Incremented on `reinforce()` call
   - **Promotion Trigger**: When `reinforcementCount >= 5`, learning promoted to Soul

3. **Soul** (DataStore, persistent):
   - Stored per `archetypeId` (alma id) as string list
   - Consolidated system prompt component
   - Built into every prompt via `memoryManager.buildContext()`
   - Never deleted (permanent learning)

**Selective Forgetting**: `olvidarDebiles()` deletes memories + learnings older than 21 days with low reinforcement.

### Memory Context Building
`buildContext(archetypeId)` returns 3-part string:
- `[SOUL — Aprendizajes consolidados]` if soul exists
- `[CONTEXTO RECIENTE]` if recent memories exist
- `[PATRONES CONFIRMADOS]` if top-5 learnings exist

This context is inserted into prompt before user message.

## Integration Points & External APIs

### Gemini API
- **Endpoint**: `https://generativelanguage.googleapis.com/v1beta/models/{modelo}:generateContent`
- **Model**: "gemini-flash-latest" (configurable in ApiService constructor)
- **Auth**: API key via `X-goog-api-key` header (loaded from `local.properties` GEMINI_API_KEY)
- **Request Format**: JSON with `contents[0].parts[0].text` (prompt), `generationConfig.temperature=0.6`, `maxOutputTokens=2048`
- **Response Parsing**: Extracts text from `candidates[0].content.parts[0].text`

### Local Storage
- **Room Database** (AppDatabase):
  - MemoryDao: CRUD for Memory (key, content, hits, timestamp)
  - LearningDao: CRUD for Learning (key, category, content, archetypeId, reinforcementCount, source)
  - Entities use Converters for serialization (GSON)
- **DataStore**: Per-archetype soul storage under key `soul_{archetypeId}`

### Gradle & Build Config
- **Build Config Field**: `BuildConfig.GEMINI_API_KEY` injected from `local.properties` at compile-time
- **KSP**: Code generation for Room DAOs
- **Dependencies**: Kotlin Compose (Jetpack Compose), Room 2.8.4, DataStore 1.2.1, Gson, Coroutines

## Developer Workflows

### Adding a New Alma
1. Create JSON in `app/src/main/res/raw/alma_{name}.json` with required fields (id, nombre, descripcion, parametros, vocabulario, microgestos, ejemplos)
2. Add mapping in `AlmaRepository.almasMap`: `"alma_{name}" to R.raw.alma_{name}`
3. Add to `cargarTodas()` list

### Testing Engine Logic
- `PersonalityEngine.procesarMensaje(userMessage, contexto)` returns `ResultadoMotor` with prompt, estado, parametros
- Direct unit tests on `StateDetector.detectar()` (no dependencies)
- Mock `AlmaRepository` and `MemoryManager` for engine tests

### Memory Debugging
- Query Room database directly: `dao.getRecentMemories(10)`, `dao.getTopReinforced(5)`
- Check soul content: `memoryManager.getSoul(archetypeId)`
- Force promotion: Set reinforcementCount=5+ manually for testing

## Project-Specific Conventions

- **Naming**: Spanish domain language (almas, estados, afinacion, alma, soul) mixed with English code structure
- **Null Safety**: Extensive use of `?:` elvis operators and `!!` for known non-nulls
- **Suspend Functions**: Memory and API operations are async-ready (even if blocking in Room)
- **Resource Loading**: Always via Context (required for assets). Caching in AlmaRepository prevents repeated file I/O
- **Error Handling**: Errors logged to Android Log.e("IAHumanizada", ...), exceptions propagated to caller
</content>
<parameter name="filePath">C:\Users\Sobet\AndroidStudioProjects\IAHumanizada\AGENTS.md
