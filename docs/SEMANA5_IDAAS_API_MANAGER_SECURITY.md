# Semana 5 - IDaaS, API Manager y Spring Security

Esta guia alinea el proyecto con la pauta de Semana 5. No corresponde crear un sistema nuevo: se reutiliza el backend trabajado en semanas anteriores y la configuracion de IDaaS/API Gateway creada en Semana 4. Sobre esa base se agrega Spring Security al microservicio Spring Boot, se redespliega en EC2 y se vuelven a inscribir/probar los endpoints en API Gateway.

## Arquitectura aplicada

Flujo esperado:

```text
Cliente / Postman
  -> AWS API Gateway
  -> Autorizador JWT con Azure AD B2C
  -> Microservicio Spring Boot en EC2
  -> Spring Security valida el mismo JWT
  -> Oracle Cloud y AWS S3
```

El API Gateway controla la entrada publica y Spring Security protege el microservicio. Esto permite evidenciar la doble proteccion indicada en la guia: una llamada directa a EC2 sin token debe responder `401`, y una llamada por API Gateway sin token tambien debe responder `401`.

## IDaaS

IDaaS usado: Azure AD B2C, reutilizando el tenant, la aplicacion y el User Flow configurados en Semana 4.

El token debe ser un JWT emitido por la politica de Azure AD B2C configurada para la aplicacion. El issuer del token se configura en el microservicio con:

```bash
AZURE_B2C_ISSUER_URI=https://TU_TENANT.b2clogin.com/TU_TENANT.onmicrosoft.com/TU_POLITICA/v2.0/
```

La misma URL debe corresponder al issuer aceptado por el autorizador JWT de API Gateway creado en Semana 4. El valor se obtiene ejecutando el User Flow y revisando el campo `iss` del token en `jwt.ms`, como indica la guia.

## Spring Security

El microservicio usa:

- `spring-boot-starter-security`
- `spring-boot-starter-oauth2-resource-server`
- validacion JWT mediante `spring.security.oauth2.resourceserver.jwt.issuer-uri`
- sesiones stateless
- CSRF deshabilitado para API REST
- todos los endpoints con `authenticated()`

Archivo principal:

- `src/main/java/cl/duoc/cdy2204/formativa/config/SecurityConfig.java`

## Endpoints que deben estar inscritos en API Gateway

| Metodo | Ruta |
| --- | --- |
| GET | `/cursos` |
| POST | `/cursos` |
| POST | `/inscripciones` |
| GET | `/inscripciones/{numeroResumen}/resumen` |
| POST | `/s3/uploadResumen?numeroResumen=1` |
| PUT | `/s3/updateResumen?numeroResumen=1` |
| GET | `/s3/downloadResumen?numeroResumen=1` |
| DELETE | `/s3/deleteResumen?numeroResumen=1` |

En API Gateway, cada ruta debe apuntar al backend Spring Boot desplegado en EC2 y debe exigir autorizador JWT.

La guia pide volver a la API creada en Semana 4, editar la integracion y apuntarla a la IP elastica de la maquina virtual con el endpoint completo. En este proyecto se debe repetir eso para cada endpoint de la tabla.

## Pasos aplicados desde la guia

1. Reutilizar las configuraciones de IDaaS de Semana 4 en Azure AD B2C.
2. Agregar dependencias de Spring Security y OAuth2 Resource Server al `pom.xml`.
3. Crear `SecurityConfig.java` dentro del paquete `config`.
4. Configurar el issuer JWT en `application.properties` con `AZURE_B2C_ISSUER_URI`.
5. Redesplegar el backend en EC2 usando GitHub Actions.
6. Probar la IP elastica de EC2 sin token y confirmar respuesta `401`.
7. Volver al API Gateway creado en Semana 4 y editar/registrar las rutas hacia la IP elastica + endpoint.
8. Desplegar nuevamente el API Gateway.
9. Probar en Postman primero sin token y luego con token emitido por Azure AD B2C.

## Evidencia Postman

Usar:

```text
docs/postman_api_gateway_semana5_collection.json
```

La coleccion esta organizada segun la guia y la pauta:

- `00 - Backend EC2 securitizado sin token`: demuestra que Spring Security bloquea llamadas directas al backend desplegado.
- `01 - API Gateway sin token`: demuestra que los endpoints responden `401 Unauthorized`.
- `02 - API Gateway con token Azure AD B2C`: demuestra que los endpoints funcionan con Bearer token valido.

Antes de ejecutar la carpeta con token:

1. Obtener el JWT desde Azure AD B2C.
2. Pegar el JWT en la variable `accessToken`.
3. Confirmar que `apiGatewayUrl` apunta al API Gateway real.
4. Confirmar que `ec2BackendUrl` apunta a la IP elastica real de EC2.

## Evidencia recomendada para el video

Orden sugerido:

1. Mostrar la arquitectura: Azure AD B2C emite JWT, API Gateway valida JWT, Spring Boot valida JWT con Spring Security.
2. Mostrar en GitHub el `SecurityConfig.java`, las dependencias de seguridad del `pom.xml` y la variable `AZURE_B2C_ISSUER_URI`.
3. Mostrar que se reutiliza el User Flow de Azure AD B2C de Semana 4 y que el issuer viene del campo `iss`.
4. Mostrar el despliegue del backend modificado en EC2.
5. Ejecutar en Postman la carpeta `00 - Backend EC2 securitizado sin token` y evidenciar `401`.
6. Mostrar API Gateway con las rutas inscritas apuntando a la IP elastica + endpoint.
7. Mostrar el autorizador JWT asociado a las rutas.
8. Ejecutar en Postman la carpeta `01 - API Gateway sin token` y evidenciar `401`.
9. Pegar el token en `accessToken`.
10. Ejecutar la carpeta `02 - API Gateway con token Azure AD B2C`.
11. Mostrar que se crean curso e inscripcion, se descarga resumen, se sube/actualiza/descarga/elimina en S3.

## Relacion con la pauta

| Criterio | Evidencia del proyecto |
| --- | --- |
| API Gateway | API Gateway reutilizado desde Semana 4, con rutas editadas hacia la IP elastica de EC2 y pruebas Postman usando `apiGatewayUrl`. |
| IDaaS | Azure AD B2C reutilizado desde Semana 4; JWT emitido por el User Flow y configurado como issuer aceptado. |
| Integracion Gateway + IDaaS | Requests sin token responden `401`; requests con token valido responden correctamente. |
| Endpoints inscritos | La coleccion cubre cursos, inscripciones, resumen local y operaciones S3 por Gateway. |
| Spring Security | `SecurityConfig.java` protege todos los endpoints como Resource Server JWT y la llamada directa a EC2 sin token responde `401`. |
| Video | Usar el orden de evidencia anterior para mostrar configuracion y funcionamiento. |
