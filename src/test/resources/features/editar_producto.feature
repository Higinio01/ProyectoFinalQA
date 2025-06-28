Feature: Editar producto en el inventario

  Scenario: Editar un producto correctamente
    Given existe un producto con nombre "Teclado", descripcion "Teclado mecánico", categoria "Periféricos", precio 45.0 y cantidad 10
    When actualizo el producto a nombre "Teclado Gamer", descripcion "Teclado retroiluminado", categoria "Periféricos", precio 55.0 y cantidad 8
    Then el producto tiene nombre "Teclado Gamer" y cantidad 8
