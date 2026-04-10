# Changelog

Todos los cambios relevantes de este proyecto se documentan en este archivo.

El formato sigue las recomendaciones de Keep a Changelog  
y el versionado utiliza Semantic Versioning.

---
## [0.0.5] - 2026-04-09

### Añadido
- Implementación inicial de la pantalla de Perfil (*Profile Screen*).
- Selector de foto de perfil con integración de cámara y galería.
- Lógica de subida de imágenes a Firebase Storage.

### Modificado
- Mejora de los permisos de red y estado de conexión.

### Corregido
- Se ha añadido el constructor sin argumentos en `UserProfile` para permitir la deserialización de Firestore.
- Corrección de dependencias en el `build.gradle` (limpieza de `animation-core-lint`).

## [0.0.4] - 2026-04-08

### Añadido
- Screen propias para Login y Register
- Screen de esqueleto para la screen Home

### Modificado
- Estetica del login y register
- Vuelvo a poner que se pueda poner contraseña

### Corregido
- Ya se recibe el correo de verificación
- El registro te manda a Welcome

## [0.0.3] - 2026-04-07

### Añadido
- Separación entre login y registro
- Inicio de sesión y registro real

### Modificado
- Componente AnimaTextField para que se pueda ver la contraseña
- Salir si no se quiere registrar o iniciar sesión

### Corregido
- La validación del inicio y registro

---

## [0.0.2] - 2026-03-27

### Añadido
- Implementación de la pantalla de login (*Login Screen*).
- Configuración de la Clean Arquitecture
- Navegación entre pantallas con Navigation Component
- Viewmodel del Login
- Componentes reutilizables (AnimaTextField, RegistrationProgressBar)

### Modificado
- La estetica de pantalla de bienvenida.

---
## [0.0.1] - 26/02/2026

### Añadido
- Pantalla Welcome
- Estructura MVVM
- Creación del repositorio en GitHub
- Documentación básica del proyecto
- Paleta de colores
- Implementacion de Hilt

### Modificado
- —

### Corregido
- —