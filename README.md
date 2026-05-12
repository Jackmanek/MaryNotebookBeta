# MaryNotebook Backend

## Descripción

MaryNotebook es una aplicación de backend construida con Spring Boot y Java 17. Proporciona APIs RESTful para gestionar recuerdos, etiquetas y usuarios.

## Características

- Registro y autenticación de usuarios.
- Operaciones CRUD para recuerdos y etiquetas.
- Paginación y ordenamiento de recuerdos.
- Filtros de recuerdos por usuario y etiqueta.
- Almacenamiento de archivos para anexos de recuerdos.

## Requisitos

- Java 17
- Maven 3.9.11 o posterior
- Base de datos Oracle (para producción) o H2 Database (para desarrollo)

## Configuración

### Ambiente de Desarrollo

1. Clona el repositorio:
   ```sh
   git clone https://github.com/your-repo/MaryNotebook.git
   cd MaryNotebook
   ```

2. Configura `application.properties` para tu entorno.

3. Construye y ejecuta la aplicación:
   ```sh
   ./mvnw spring-boot:run
   ```

### Ambiente de Producción

1. Clona el repositorio:
   ```sh
   git clone https://github.com/your-repo/MaryNotebook.git
   cd MaryNotebook
   ```

2. Configura `application-prod.properties` para tu entorno de producción.

3. Construye la aplicación:
   ```sh
   ./mvnw clean package -DskipTests
   ```

4. Despliega el JAR file en tu servidor.

5. Utiliza el Dockerfile proporcionado para crear una imagen Docker:

   ```sh
   docker build -t marynotebook:latest .
   ```

6. Ejecuta la aplicación dentro del contenedor:

   ```sh
   docker run -d -p 8080:8080 --name marynotebook-container marynotebook:latest
   ```

## Documentación de API

La documentación de la API está disponible en `/swagger-ui.html`.

Para acceder a la documentación, asegúrate de que tu aplicación esté ejecutándose y luego navega a:
