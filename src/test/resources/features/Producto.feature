Feature: Gestionar productos en el inventario

  Scenario: Crear un producto correctamente
    Given el inventario de productos está vacío
    When creo un producto con nombre "Mouse", descripcion "Mouse inalámbrico", categoria "TECNOLOGIA", precio 20.5 y cantidad 15
    Then el producto "Mouse" aparece en la lista de productos

  Scenario: Editar un producto correctamente
    Given existe un producto con nombre "Teclado", descripcion "Teclado mecánico", categoria "Periféricos", precio 45.0 y cantidad 10
    When actualizo el producto a nombre "Teclado Gamer", descripcion "Teclado retroiluminado", categoria "Periféricos", precio 55.0 y cantidad 8
    Then el producto tiene nombre "Teclado Gamer" y cantidad 8

  Scenario: Eliminar un producto correctamente
    Given existe un producto para eliminar con nombre "Audífonos", descripcion "Audífonos bluetooth", categoria "Periféricos", precio 35.0 y cantidad 12
    When elimino el producto
    Then el producto ya no existe en la base de datos

  Scenario: No se puede crear un producto con una categoría inválida
    Given el inventario de productos está vacío
    When creo un producto con nombre "Silla", descripcion "Silla ergonómica", categoria "INVALIDA", precio 50.0 y cantidad 5
    Then ocurre un error al crear el producto con el mensaje "No enum constant org.example.Entity.Categoria.INVALIDA"

  Scenario: No se puede eliminar un producto inexistente
    When intento eliminar un producto inexistente
    Then ocurre un error al eliminar el producto con el mensaje "No class org.example.Entity.Producto entity with id -1 exists!"
