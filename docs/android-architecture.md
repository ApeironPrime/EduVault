# Android architecture

The Android app follows a single-module, feature-first clean architecture while the project remains small enough that Gradle multi-modules would add more overhead than value.

```
feature/*/ui  ->  domain/usecase  ->  domain/repository  <-  data/repository
                         |                                      |
                    domain/model                         Firebase SDKs
```

## Rules now enforced by structure

- Repository contracts and shared business models live under `domain`; they do not import feature UI classes.
- Firebase mapping and SDK calls belong to `data/repository`.
- UI-specific `LibraryDoc` is retained only as a rendering model. `LibraryDocumentMapper` is the explicit feature boundary.
- Shared reads used by Home and Quiz go through `GetPublishedDocumentsUseCase` rather than directly coupling those ViewModels to the data source.
- Flashcard access has its own `FlashcardRepository`; its Firebase document mapping is isolated from the UI.
- Hilt bindings stay centralised in `AppModule`; `MainActivity` only bootstraps Compose/navigation.

## Next extraction increments

1. Split each very large screen into `screen`, `components`, and `dialogs` files without changing public route signatures.
2. Replace remaining legacy direct Firebase calls inside library/home composables with repository-backed ViewModels.
3. Move `LibraryDoc` and other rendering-only models into per-feature `ui/model` packages, then migrate each feature to domain models plus mappers.
4. Introduce Gradle modules (`core`, `domain`, `data`, feature modules) only after the circular feature dependencies are removed; this preserves build speed during migration.

This staged approach avoids a risky big-bang package move while creating enforceable boundaries for all new code.
