Credencial inicial para entrar 
Correo
admin@ucv.ve
contrasena
123


Persistencia de datos
Los datos se guardan en archivos .txt dentro de la carpeta "datos", que se crea sola junto al lugar desde donde se ejecuta el programa (se puede cambiar con -Druta.datos=otra_carpeta). Cada línea del archivo es un objeto y sus campos van separados por "|".

usuarios.txt  USUARIO|rol|nombre|apellido|cedula|correo|contrasena
unidades.txt  UNIDAD|placa|modelo|capacidad|estado
rutas.txt     RUTA|id|tipoRuta|idRuta|origen|destino|detalle|cedulaChofer|nombreChofer|licenciaChofer|placa|modeloUnidad|capacidadUnidad|estadoUnidad|dia|horaSalida|horaLlegada|estado

Si se borra la carpeta "datos", el programa arranca vacío (con el administrador por defecto).
