## Escuela Colombiana de Ingeniería
### Arquitecturas Empresariales – AREP
# LAB4-AREP
#### TALLER 4: TALLER DE ARQUITECTURAS DE SERVIDORES DE APLICACIONES, META PROTOCOLOS DE OBJETOS, PATRÓN IOC, REFLEXIÓN
Para este taller los estudiantes deberán construir un servidor Web (tipo Apache) en Java. El servidor debe ser capaz de entregar páginas html e imágenes tipo PNG. Igualmente el servidor debe proveer un framework IoC para la construcción de aplicaciones web a partir de POJOS. Usando el servidor se debe construir una aplicación Web de ejemplo. El servidor debe atender múltiples solicitudes no concurrentes.

Para este taller desarrolle un prototipo mínimo que demuestre capcidades reflexivas de JAVA y permita por lo menos cargar un bean (POJO) y derivar una aplicación Web a partir de él. 

## Elementos necesarios 
Para poder ejecutar o correr el proyecto se necesitan unos requisitos minimos los cuales son:
* [Tener Instalado Maven](https://maven.apache.org/download.cgi)
* [Git](https://git-scm.com/downloads)
* [Tener una version de Java 17 o mas](https://www.oracle.com/co/java/technologies/downloads/)

## La aplicacion cuenta con 4 clases principales las cuales son

**MovieServer**: Esta clase implementa un servidor web básico que utiliza reflexión para gestionar solicitudes HTTP. Además, se integra con clases anotadas con @Component que contienen métodos anotados con @RequestMapping, siguiendo el estilo de programación de Spring Framework.

**MovieController**: Esta clase actúa como un controlador de películas que gestiona las solicitudes a la ruta "/movies", utiliza un sistema de almacenamiento en caché para optimizar el rendimiento y se integra con un proveedor de datos de películas para obtener información actualizada. Además, está diseñada para trabajar en un entorno de aplicación web con Spring Framework.

**ApiRestMovies**: Esta clase se encarga de realizar solicitudes HTTP al servicio web OMDb para obtener información de películas a través del método fetchMovieData. La información recuperada se devuelve en formato JSON. Además, se implementa una lógica de manejo de errores para registrar cualquier problema que pueda surgir durante la solicitud.

**HelloController**: Esta clase actúa como un controlador de Spring para gestionar solicitudes HTTP en la ruta "/hola". Cuando se recibe una solicitud en esta ruta, devuelve un saludo HTML personalizado utilizando el nombre proporcionado en los parámetros de la solicitud. La configuración del tipo de respuesta a "text/html" sugiere que el resultado es una página HTML.

## POR CONSOLA
Por consola nos metemos hasta la carpeta Lab1 del proyecto y desde ahi ponemos los siguientes comandos

* mvn clean compile
* mvn exec:java
  
El primer comando nos ayudara a compilar el proyecto y el segundo a ejecutarlo

## DIRECTAMENTE DEL IDLE
Si queremos ejecutarlo de la otra forma solo tenemos que correr la clase Main con ayuda de nuestro IDLE

## NOTA:
En los dos casos lo mas recomendable para ver el funcionamiento de la pagina es utilizar el Navegador de FireFox
