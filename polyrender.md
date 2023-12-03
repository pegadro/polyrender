# PolyRender: Arte Poligonal Generativo

Autor: Pedro Alvarado García

---

![](image1.png)

## Introducción
PolyRender es una herramienta de procesamiento de imágenes que utiliza algoritmos avanzados para transformar imágenes en obras de arte únicas compuestas por polígonos. A través de la técnica de segmentación por superpíxeles y la creación de envolventes convexas, el software reinventa visualmente las imágenes originales, ofreciendo una nueva perspectiva artística que se sitúa en la intersección de la tecnología y el arte digital.

## Inspiración
Este proyecto se inspira en unos trabajos de naturaleza similar. A continuación se dejan enlaces a tales proyectos:
1. [Primitive](https://github.com/fogleman/primitive)
2. [Geometrize](https://github.com/Tw1ddle/geometrize)
3. [Genetic-lisa](https://github.com/peterbraden/genetic-lisa)
4. [Evolution of Mona Lisa](https://rogerjohansson.blog/2008/12/07/genetic-programming-evolution-of-mona-lisa/)

## Cómo funciona
PolyRender transforma imágenes estándar en obras de arte poligonales únicas. Este proceso involucra varios pasos clave, desde la segmentación inicial de la imagen hasta la creación final de arte poligonal.

### Paso 1: Selección y preparación de la imagen
Inicialmente en el programa el usuario debe abrir una imagen. Posteriormente, para que el algoritmo sea más eficiente se reescala la imagen.

### Paso 2: Segmentación por superpíxeles
Un superpixel es un grupo de píxeles adyacentes en una imagen que comparten características similares, como color, intensidad, o textura. Los superpixels son útiles porque reducen la complejidad de una imagen, agrupando píxeles en unidades más grandes y manejables que conservan mucha de la información estructural importante.

En la implementación, la segmentación por superpíxeles se lleva a cabo a través del algoritmo SLIC(Simple Linear Iterative Clustering).

En términos sencillos, SLIC funciona dividiendo una imagen en pequeñas áreas llamadas superpíxeles. Estos superpíxeles son como pequeñas piezas del rompecabezas que juntas forman la imagen completa. SLIC elige puntos iniciales en la imagen y agrupa los píxeles cercanos que se parecen entre sí en color y posición. Luego, ajusta estos grupos varias veces hasta que los superpíxeles forman una representación clara y eficiente de la imagen, asegurándose de que cada pieza del rompecabezas encaje bien con sus vecinas. Este método es rápido y efectivo, y ayuda a simplificar la imagen para análisis posteriores.

![](slic_example.png)
<figure>
    <figcaption>Ejemplo de superpixeles de una imagen tras aplicar SLIC (aproximadamente 250 segmentos)</figcation>
</figure>

### Paso 3: Creación de envolventes convexas (convex hulls)
Una envolvente convexa es, en términos sencillos, como una banda elástica estirada para rodear un grupo de objetos. Imagina que tienes varios clavos clavados en una tabla y estiras una banda elástica para que rodee todos los clavos. La forma que toma la banda elástica representa la envolvente convexa de los clavos.

![](convex_hull.png)

La librería que se usa para implementar la generación de envolventes convexas usa el algoritmo de Graham Scan.

![](graham_scan.gif)

Para nuestro caso particular cada grupo de objetos serán los superpixeles. Cada uno de los superpixeles obtenidos con SLIC son potenciales polígonos. Por cada superpixel se genera una envolvente convexa que encierra a todos los puntos del superpixel.

Los puntos de cada envolvente convexa serán los puntos de los polígonos.

![](convex_hull_example.png)
<figure>
    <figcaption>Ejemplo de envolventes convexas. Cada envolvente convexa representa un superpixel y envuelve a todos los puntos de ese superpixel</figcation>
</figure>

### Paso 4: Renderizado de polígonos
Una vez que ya tenemos las envolventes convexas y por tanto, los polígonos, procedemos a renderizar cada polígono. Los puntos de los polígonos están dados por los puntos de la envolvente convexa y el color del polígono está dado por el color promedio de los pixeles del superpixel asociado a tal polígono y a su envolvente convexa.

![](example.gif)

## Instalación

Requerimientos previos: Tener instalado el JRE (Java Runtime Enviroment) y python3.

1. Descargar el ejecutable polyrender.jar
2. Descagar main.py y el archivo requirements.txt.
3. De preferencia crear un entorno virtual de python para descargar las librerías que contiene requirements.txt.
4. Para que la aplicación de Java funcione debe estar previamente corriendo la aplicación de Python. Para esto solo corra el programa de python con `python3 main.py` (asegurese de que está encendido el entorno virtual y que las librerías están descargadas).