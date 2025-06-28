Feature: Agregar producto al inventario

  Como administrador
  Quiero agregar un nuevo producto con nombre, descripción, categoría, precio y cantidad
  Para gestionar correctamente el inventario

  Scenario: Crear un producto correctamente
    Given el inventario de productos está vacío
    When creo un producto con nombre "Mouse", descripcion "Mouse inalámbrico", categoria "Periféricos", precio 20.5 y cantidad 15
    Then el producto "Mouse" aparece en la lista de productos