# PosicionMaximosIntensidad

Plugin para [ImageJ/Fiji](https://fiji.sc/) desarrollado como parte de un proyecto de investigación en física médica de la [Universidad de Costa Rica (UCR)](https://www.ucr.ac.cr/). Permite extraer y registrar la posición de los máximos de intensidad en stacks de imágenes DICOM adquiridas con el escáner **PET/CT Biograph Vision 450 Edge** del Centro de Investigación en Ciencias Atómicas, Nucleares y Moleculares (CICANUM-UCR).

---

## Contexto del proyecto

El proyecto evalúa el **movimiento continuo de la camilla** del PET/CT Biograph Vision 450 Edge ante variaciones de velocidad de escaneo. Para ello se utilizan cuatro fuentes radiactivas posicionadas en los vértices de un cuadrado sobre la camilla, formando una configuración geométrica de referencia. Este plugin analiza los stacks DICOM resultantes y cuantifica el desplazamiento de dichas fuentes entre cortes (slices), lo que permite caracterizar la estabilidad del movimiento de la camilla.

---

## Requisitos

| Componente | Versión recomendada |
|---|---|
| [Fiji / ImageJ](https://fiji.sc/) | 2.x o superior |
| Java | 8 o superior (incluido en Fiji) |
| Imágenes de entrada | Stack DICOM del PET/CT (modalidad PET reconstruida) |

No requiere dependencias externas ni librerías adicionales. Utiliza únicamente la API estándar de ImageJ (`ij.*`).

---

## Instalación

1. Descargar el archivo `PosicionMaximosIntensidad.java`.
2. Compilar el plugin directamente desde Fiji:
   - Abrir Fiji.
   - Ir a **Plugins → Compile and Run...** y seleccionar el archivo `.java`.
   - Fiji compilará e instalará el plugin automáticamente.
3. Alternativamente, colocar el archivo `.java` en la carpeta `plugins/` del directorio de instalación de Fiji y reiniciar la aplicación.

---

## Uso

### Preparación

1. Abrir el stack DICOM en Fiji (**File → Import → Image Sequence** o arrastrar la carpeta).
2. Dibujar una **ROI rectangular** (`Rectangle Tool`) sobre la región de interés que contiene las cuatro fuentes. La ROI debe abarcar todas las fuentes en todos los slices del stack.

### Ejecución

1. Ir a **Plugins → PosicionMaximosIntensidad** (o usar **Plugins → Compile and Run...** si se usa el `.java` directamente).
2. El plugin ejecutará tres etapas de forma secuencial:

### Etapa 1 — Identificación del píxel de referencia global

El plugin recorre todos los slices del stack dentro de la ROI y localiza el píxel de mayor valor de intensidad en todo el volumen. Las coordenadas resultantes `(x*, y*)` se registran en el **Log de ImageJ** y sirven como planos de referencia fijos para los análisis siguientes.

### Etapa 2 — Perfil en el plano Y\* (archivo `y_plane_profile.csv`)

Para cada slice `z`, el plugin fija la fila `y*` y recorre horizontalmente la ROI en busca del píxel de máxima intensidad. Se genera:
- Un archivo CSV con las columnas `z`, `x_max`, `intensity_max`.
- Una gráfica de `x_max` vs. `z` en la interfaz de ImageJ.

Este perfil representa el **desplazamiento transversal** de las fuentes a lo largo del escaneo.

### Etapa 3 — Perfil en el plano X\* (archivo `x_plane_profile.csv`)

Análogamente, fija la columna `x*` y recorre verticalmente la ROI. Se genera:
- Un archivo CSV con las columnas `z`, `y_max`, `intensity_max`.
- Una gráfica de `y_max` vs. `z` en la interfaz de ImageJ.

Este perfil representa el **desplazamiento axial** de las fuentes a lo largo del escaneo.

---

## Descripción de los archivos de salida

| Archivo | Columnas | Descripción |
|---|---|---|
| `y_plane_profile.csv` | `z`, `x_max`, `intensity_max` | Posición horizontal del máximo de intensidad en la fila `y*` para cada slice |
| `x_plane_profile.csv` | `z`, `y_max`, `intensity_max` | Posición vertical del máximo de intensidad en la columna `x*` para cada slice |

> **Nota:** las coordenadas están expresadas en píxeles relativos a la esquina superior izquierda de la ROI. Para convertirlas a distancias físicas (mm) es necesario aplicar el valor `PixelSpacing` disponible en el encabezado DICOM de las imágenes.

---

## Estructura del código

```
PosicionMaximosIntensidad.java
│
├── run(String arg)                  ← Punto de entrada del plugin
│   ├── Lectura de la imagen e validación de ROI
│   ├── Barrido global → obtención de (x*, y*)
│   ├── Perfil Y*
│   │   ├── Barrido por slice en fila y*
│   │   ├── Escritura de y_plane_profile.csv
│   │   └── Gráfica x_max vs. z
│   └── Perfil X*
│       ├── Barrido por slice en columna x*
│       ├── Escritura de x_plane_profile.csv
│       └── Gráfica y_max vs. z
```

---

## Consideraciones y limitaciones

- **El píxel de referencia `(x*, y*)` es estático.** Se calcula una sola vez al inicio del análisis. Si el movimiento de la camilla es significativo, el plano de barrido podría desplazarse respecto a la posición real de las fuentes en slices alejados del máximo global.
- **La ROI es determinante.** Si alguna fuente queda fuera de la ROI en algún slice, los valores de `x_max` o `y_max` en ese slice no corresponderán a ninguna fuente real. Se recomienda definir una ROI conservadora.
- **Indexación de slices.** Los archivos CSV usan indexación base 0 (el slice 1 de Fiji se guarda como `z = 0`).
- **Unidades en píxeles.** Todos los resultados de posición están en coordenadas de píxel. La conversión a unidades físicas requiere el `PixelSpacing` del encabezado DICOM.

---

## Autores y afiliación

Desarrollado en el marco del proyecto de investigación sobre control de calidad del PET/CT Biograph Vision 450 Edge por Iván Rojas Duarte (Desarrollador de código), Jimena Hernández Tames (Documentación) y el supervisor Erick Mora Ramírez

**Escuela de Física — Universidad de Costa Rica**
Centro de Investigación en Ciencias Atómicas, Nucleares y Moleculares (CICANUM-UCR)

---

## Licencia

Este software se distribuye con fines académicos y de investigación. Para cualquier uso fuera de este contexto, consultar con los autores.
