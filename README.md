# Bit Chronicles 🐉

![Versión](https://img.shields.io/badge/versión-1.0-blue)
![Plataforma](https://img.shields.io/badge/plataforma-Android-brightgreen)

**Bit Chronicles** es una aplicación móvil para Android, desarrollada en **Kotlin y Java**, que reinventa la forma de gestionar y jugar campañas de Rol (RPG). Utiliza la potencia de la **IA de Google (Gemini)** para actuar como un Dungeon Master dinámico, generar narrativas inmersivas y crear representaciones visuales de las escenas, todo sincronizado en tiempo real a través de **Firebase**.

---
## 📜 Tabla de Contenidos
1. [Características Principales](#-características-principales)
2. [Capturas de Pantalla](#-capturas-de-pantalla)
3. [Tecnologías Utilizadas](#-tecnologías-utilizadas)
4. [Arquitectura del Sistema](#-arquitectura-del-sistema)
5. [Instalación y Puesta en Marcha](#-instalación-y-puesta-en-marcha)
6. [Estructura del Proyecto](#-estructura-del-proyecto)
7. [Autor](#-autor)

---
## ✨ Características Principales
* **🤖 Dungeon Master IA:** Interactúa con una IA (Gemini) que dirige la partida, describe escenas y reacciona a las acciones del jugador.
* **🎨 Creación Asistida por IA:** Genera campañas y personajes completos a partir de datos iniciales proporcionados por el usuario.
* **🗺️ Mapa Visual Generado por IA:** La narrativa se traduce en una matriz visual que representa el entorno del juego en tiempo real.
* **☁️ Sincronización en la Nube:** Todos los datos de campañas, personajes y partidas se almacenan y sincronizan con **Firebase Firestore**.
* **🔐 Autenticación de Usuarios:** Sistema de login y registro seguro mediante **Firebase Authentication**.
* **🎲 Utilidades de Juego:** Incluye funcionalidades adicionales como un lanzador de dados integrado.

---
## 📱 Capturas de Pantalla
*(Aquí puedes poner imágenes de tu aplicación. Sube las imágenes a tu repositorio y enlaza a ellas)*

| Pantalla de Inicio | Creación de Campaña | Pantalla de Juego |
| :---: |:---:|:---:|
| ![Pantalla de Inicio](URL_DE_TU_IMAGEN_AQUI) | ![Creación de Campaña](URL_DE_TU_IMAGEN_AQUI) | ![Pantalla de Juego](URL_DE_TU_IMAGEN_AQUI) |

---
## 🛠️ Tecnologías Utilizadas
* **Lenguajes:** [Kotlin](https://kotlinlang.org/) y [Java](https://www.java.com/)
* **Plataforma:** [Android Nativo](https://developer.android.com/)
* **Arquitectura:** **MVVM** (Model-View-ViewModel)
* **Base de Datos y Backend:** [Firebase](https://firebase.google.com/) (Firestore, Authentication)
* **Inteligencia Artificial:** [Google Gemini API](https://ai.google.dev/)
* **UI:** Android XML Layouts, Material Design
* **Componentes de Arquitectura:** LiveData, ViewModel, Navigation Component

---
## 🏛️ Arquitectura del Sistema
La aplicación sigue una arquitectura MVVM robusta, separando la UI, la lógica de presentación y el modelo de datos. El `Repository` centraliza la comunicación con los servicios de Firebase y Gemini.

*(GitHub renderizará automáticamente este código Mermaid en un diagrama)*
mermaid
graph TD
subgraph "Cliente: Bit Chronicles (Kotlin/Java)"
direction TB
V(View
Activities/Layouts) <--> VM(ViewModel
viewmodel/)
VM --> R(Repository
model/)
end

subgraph "Backend: Google Cloud"
    direction TB
    FB_Auth[Firebase Authentication]
    FB_DB[Firestore Database]
    Gemini[Gemini API]
end

User[(Jugador)] -- Interactúa con --> V

R -- Petición de Login/Registro --> FB_Auth
R -- Lectura/Escritura de Datos --> FB_DB
R -- Petición de Generación de Contenido --> Gemini

---
## 🚀 Instalación y Puesta en Marcha
Sigue estos pasos para configurar y ejecutar el proyecto en tu entorno local.

### **Pre-requisitos**
* [Android Studio](https://developer.android.com/studio) (versión Iguana o superior)
* JDK 17 o superior

### **Pasos**
1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/TU_USUARIO/Bit-Chronicles.git](https://github.com/TU_USUARIO/Bit-Chronicles.git)
    ```

2.  **Configurar Firebase:**
    * Ve a la [consola de Firebase](https://console.firebase.google.com/) y crea un nuevo proyecto.
    * Añade una nueva aplicación Android con el nombre del paquete `com.bit_chronicles` (o el que corresponda a tu proyecto).
    * Descarga el archivo `google-services.json` y colócalo en el directorio `app/` de tu proyecto.
    * Habilita los servicios de **Authentication** (con el proveedor Email/Password) y **Firestore Database**.

3.  **Configurar la API Key de Gemini:**
    * Obtén tu API key desde [Google AI Studio](https://aistudio.google.com/app/apikey).
    * Abre el archivo `local.properties` en la raíz de tu proyecto (si no existe, créalo).
    * Añade tu API key en una línea, así:
        ```properties
        GEMINI_API_KEY="TU_API_KEY_AQUI"
        ```

4.  **Abrir y Ejecutar:**
    * Abre el proyecto en Android Studio.
    * Sincroniza el proyecto con los archivos de Gradle.
    * Ejecuta la aplicación en un emulador o dispositivo físico.

---
## 📁 Estructura del Proyecto
El código está organizado siguiendo los principios de la arquitectura limpia y MVVM:

* **`com.bit_chronicles.model`**: Contiene las clases de datos (`ChatMessage`, `User`), los servicios que interactúan con Firebase y Gemini, y el `Repository`.
* **`com.bit_chronicles.viewmodel`**: Contiene los `ViewModels` para cada pantalla, organizados por funcionalidad (`auth`, `campaign`, `map`, etc.).
* **`com.bit_chronicles.view` (implícito):** Las `Activities` y `Fragments` que componen la capa de la Vista y utilizan los layouts de la carpeta `res/layout`.

---
## 👨‍💻 Autor
*  **Maycol Andrei Figueroa**
* **Diego Alejandro Hurtado**
* **Julian Santiago Barbosa**



---

