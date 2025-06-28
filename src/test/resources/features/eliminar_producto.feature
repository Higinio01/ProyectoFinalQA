Feature: Eliminar producto del inventario

  Scenario: Eliminar un producto correctamente
    Given existe un producto para eliminar con nombre "Audífonos", descripcion "Audífonos bluetooth", categoria "Periféricos", precio 35.0 y cantidad 12
    When elimino el producto
    Then el producto ya no existe en la base de datos
