Feature: Gestionar productos en el inventario

  Scenario: Crear un producto correctamente
    Given el inventario de productos está vacío
    When creo un producto con nombre "Mouse", descripcion "Mouse inalámbrico", categoria "ELECTRONICA", precio 20.5 y cantidad 15
    Then el producto "Mouse" aparece en la lista de productos

  Scenario: Editar un producto correctamente
    Given existe un producto con nombre "Teclado", descripcion "Teclado mecánico", categoria "ELECTRONICA", precio 45.0 y cantidad 10
    When actualizo el producto a nombre "Teclado Gamer", descripcion "Teclado retroiluminado", categoria "ELECTRONICA", precio 55.0 y cantidad 8
    Then el producto tiene nombre "Teclado Gamer" y cantidad 8

  Scenario: Eliminar un producto correctamente
    Given existe un producto para eliminar con nombre "Audífonos", descripcion "Audífonos bluetooth", categoria "ELECTRONICA", precio 35.0 y cantidad 12
    When elimino el producto
    Then el producto ya no existe en la base de datos

  Scenario: No se puede crear un producto con una categoría inválida
    Given el inventario de productos está vacío
    When creo un producto con nombre "Silla", descripcion "Silla ergonómica", categoria "INVALIDA", precio 50.0 y cantidad 5
    Then ocurre un error al crear el producto con el mensaje "Categoría inválida: INVALIDA"

  Scenario: No se puede eliminar un producto inexistente
    When intento eliminar un producto inexistente
    Then el producto no existe y no ocurre error

  Scenario: No se puede crear un producto con precio negativo
    Given el inventario de productos está vacío
    When creo un producto con nombre "Laptop", descripcion "Laptop gaming", categoria "ELECTRONICA", precio -1000.0 y cantidad 5
    Then ocurre un error al crear el producto con el mensaje "El precio no puede ser negativo: -1000.0"

  Scenario: No se puede crear un producto con cantidad negativa
    Given el inventario de productos está vacío
    When creo un producto con nombre "Laptop", descripcion "Laptop gaming", categoria "ELECTRONICA", precio 1000.0 y cantidad -5
    Then ocurre un error al crear el producto con el mensaje "La cantidad no puede ser negativa: -5"

  Scenario: No se puede actualizar un producto con precio negativo
    Given existe un producto con nombre "Tablet", descripcion "Tablet Android", categoria "ELECTRONICA", precio 200.0 y cantidad 20
    When actualizo el producto a nombre "Tablet Pro", descripcion "Tablet Android Pro", categoria "ELECTRONICA", precio -150.0 y cantidad 15
    Then ocurre un error al actualizar el producto con el mensaje "El precio no puede ser negativo: -150.0"

  Scenario: No se puede actualizar un producto con cantidad negativa
    Given existe un producto con nombre "Tablet", descripcion "Tablet Android", categoria "ELECTRONICA", precio 200.0 y cantidad 20
    When actualizo el producto a nombre "Tablet Pro", descripcion "Tablet Android Pro", categoria "ELECTRONICA", precio 150.0 y cantidad -5
    Then ocurre un error al actualizar el producto con el mensaje "La cantidad no puede ser negativa: -5"

  Scenario: Obtener un producto por ID existente
    Given existe un producto con nombre "Mouse", descripcion "Mouse óptico", categoria "ELECTRONICA", precio 15.0 y cantidad 30
    When solicito el producto por su ID
    Then el producto obtenido tiene nombre "Mouse"

  Scenario: No se puede obtener un producto por ID inexistente
    When solicito el producto con ID -1
    Then ocurre un error al obtener el producto con el mensaje "Producto no encontrado con id: -1"