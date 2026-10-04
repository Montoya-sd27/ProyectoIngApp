package com.transporte.model;

// clase padre, todos los usuarios heredan de aqui 
public abstract class Usuario {
    
    // atributos que tendrá cualquier usuario
    protected String nombre;
    protected String apellido;
    protected String cedula;
    protected String correo;
    protected String contrasena;
    protected String rol;

    // constructor
    public Usuario(String nombre, String apellido, String cedula, String correo, String contrasena, String rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // métodos para obtener los datos 
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getCedula() { return cedula; }
    public String getCorreo() { return correo; }
    public String getContrasena() { return contrasena; }
    public String getRol() { return rol; }
}