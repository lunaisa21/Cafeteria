# Cafetería - Sistema de Gestión de Pedidos

Sistema de gestión de pedidos para un local con productos personalizables, desarrollado en Java bajo principios de Programación Orientada a Objetos y patrones de diseño.

## Componentes técnicos
* **Lenguaje:** JDK 21
* **Gestor de dependencias:** Maven 3.2
* **Desarrollado por:** Luna Isabella Martinez Marthos, Daniel Alfonso Mateus Neva, Harold David Higuera Torres
* **Asesorado por:** ..
* **Versión:** 1.0

## Alcance del Proyecto

### Funcionalidades Incluidas
1. **Gestión de Menú y Personalización:** Adiciones a platos y bebidas que modifican dinámicamente el precio y la descripción (RQ02, RQ03, RQ08).
2. **Armado de Combos:** Creación de combos paso a paso (RQ04).
3. **Flujo y Estados del Pedido:** Ciclo de vida del pedido (Pendiente, En preparación, Listo, etc.) con alertas automáticas a cocina, caja y mesero (RQ06, RQ09, RQ11).
4. **Motor de Promociones:** Aplicación de descuentos según día de la semana y franja horaria (RQ05).
5. **Gestión de Inventario:** Descuento automático de stock al confirmar pedidos, bloqueo de productos agotados y alertas de stock mínimo (RF12, RF13, RF14).
6. **Cierre de Caja:** Reporte de ventas diario y cálculo de totales (RQ07, RF10).
7. **Doble Interfaz:** Núcleo de negocio independiente que alimenta tanto a la interfaz de consola como a la interfaz gráfica (GUI).

### Funcionalidades Excluidas (y su razón)
* **Pasarelas de pago reales:** El sistema simulará los pagos (efectivo/tarjeta) para enfocar el esfuerzo en la arquitectura y patrones, no en integraciones bancarias.
* **Base de Datos relacional (SQL):** Se usará persistencia en archivos locales (JSON/TXT) para cumplir el objetivo académico de centrarnos en el diseño orientado a objetos.
* **Autenticación avanzada:** Solo se implementará un login básico para diferenciar roles (Administrador vs Mesero/Cajero), sin llegar a estándares de producción.

## Documentación Adicional
Toda la documentación profunda sobre la arquitectura, problemas de diseño, justificación de patrones (Strategy, Observer, Decorator, Builder) y los diagramas UML se encuentran en la pestaña **[Wiki](https://github.com/lunaiza21/Cafeteria/wiki)** de este repositorio.

## Cómo compilar y ejecutar
(Faltaria colocar las instrucciones para compilar el codigo :D)
1. Clonar el repositorio.
2. Ejecutar `..` en la raíz del proyecto.
3. Para ejecutar la versión de consola: `..`
4. Para ejecutar la versión gráfica: `..`
