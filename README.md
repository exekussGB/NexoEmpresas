# NexoEmpresas — Android App

App de gestión financiera para contadores y empresas (Chile).

## Setup

1. **Clonar / abrir en Android Studio**
2. **Copiar credenciales:**
   ```
   cp local.properties.template local.properties
   # Editar local.properties con tu SUPABASE_URL y SUPABASE_ANON_KEY
   ```
3. **Firebase:** Agregar tu `google-services.json` en `app/`
4. **Sync Gradle** → Run

## Stack

| Capa | Tecnología |
|------|-----------|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Backend | Supabase (Auth + PostgREST + Storage) |
| Navegación | Navigation Compose |
| Gráficos | Vico |
| Imágenes | Coil |
| Push | Firebase Cloud Messaging |

## Estructura de paquetes

```
cl.nexo.empresas/
├── core/
│   ├── di/          # Hilt modules
│   ├── navigation/  # NavGraph + Screen sealed class
│   └── util/        # Constants
├── data/
│   ├── model/       # Data classes (Supabase schema)
│   └── repository/  # RepositoryImpl
├── domain/
│   └── repository/  # Repository interfaces
└── presentation/
    ├── auth/        # Login, Register, AuthViewModel
    ├── empresas/    # Lista y selección de empresa
    ├── hub/         # Menú principal
    ├── dashboard/   # Resumen financiero
    ├── documentos/  # CxC, CxP, AddDocumento
    ├── cheques/     # Gestión de cheques
    ├── cuentas/     # Cuentas corrientes
    ├── graficos/    # Charts con Vico
    ├── opciones/    # Settings
    └── theme/       # Colores y tipografía
```

## Módulos pendientes (ver PLAN_MAESTRO.md)
- Módulo 3: Dashboard (RPCs Supabase)
- Módulo 4: Documentos (CxC/CxP + formulario)
- Módulo 5: Cheques
- Módulo 6: Cuentas Corrientes
- Módulo 7: Gráficos (Vico)
- Módulo 8: Alertas FCM
- Módulo 9: Opciones/Settings
