package com.acainfo.material.domain.model;

import lombok.Getter;

/**
 * Categorías predefinidas para organizar materiales educativos.
 * Permite una organización simple sin necesidad de estructura jerárquica compleja.
 */
@Getter
public enum MaterialCategory {
    TEORIA("Teoría", "teoria"),
    EJERCICIOS("Ejercicios", "ejercicios"),
    EXAMENES("Exámenes", "examenes"),
    PROYECTOS("Proyectos", "proyectos"),
    LABORATORIOS("Laboratorios", "laboratorios"),
    OTROS("Otros", "otros");

    private final String displayName;
    private final String folderName;

    MaterialCategory(String displayName, String folderName) {
        this.displayName = displayName;
        this.folderName = folderName;
    }

    /**
     * Convierte un string a MaterialCategory.
     * Si el valor es null o inválido, retorna OTROS por defecto.
     */
    public static MaterialCategory fromString(String value) {
        if (value == null) {
            return OTROS;
        }

        try {
            return MaterialCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTROS;
        }
    }

    /**
     * Verifica si este es el valor por defecto.
     */
    public boolean isDefault() {
        return this == OTROS;
    }
}
