Credencial inicial para entrar 
Correo
admin@ucv.ve
contrasena
123


Persistencia de datos
Los datos se guardan en archivos .txt dentro de la carpeta "datos", que se crea sola junto al lugar desde donde se ejecuta el programa (se puede cambiar con -Druta.datos=otra_carpeta). Cada línea del archivo es un objeto y sus campos van separados por "|".

usuarios.txt  USUARIO|rol|nombre|apellido|cedula|correo|contrasena   (estudiante y empleado llevan al final |saldo)
unidades.txt  UNIDAD|placa|modelo|capacidad|estado
rutas.txt     RUTA|id|tipoRuta|idRuta|origen|destino|detalle|cedulaChofer|nombreChofer|licenciaChofer|placa|modeloUnidad|capacidadUnidad|estadoUnidad|dia|horaSalida|horaLlegada|estado

Si se borra la carpeta "datos", el programa arranca vacío (con el administrador por defecto).

Recarga de saldo por pago móvil
El estudiante y el empleado recargan su saldo con el botón "Recargar saldo" de su pantalla de inicio: se muestran los datos del pago móvil y, con "Reportar pago", se llena el formulario (teléfono, cédula, banco y últimos 4 dígitos de la referencia). El pago se valida contra el archivo datos/pagos_movil.txt, que simula los pagos recibidos por el banco:

pagos_movil.txt  PAGO|telefono|cedula|banco|ultimos4Referencia|monto|estado   (estado: PENDIENTE o ACREDITADO)

Si existe un pago PENDIENTE con esos datos, su monto se suma al saldo y la línea pasa a ACREDITADO (no se puede usar dos veces). La primera vez que se ejecuta el programa, el archivo se crea con tres pagos de ejemplo; para simular otro pago basta con agregar una línea nueva.
