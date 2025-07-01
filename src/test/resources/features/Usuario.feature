Feature: Gestion de usuarios en el sistema

  Scenario: Crear un usuario correctamente
    Given existe un rol con nombre "CLIENTE"
    When creo un usuario con nombre "Sara", apellido "Contreras", email "sara@example.com", password "password123" y rol "CLIENTE"
    Then el usuario con email "sara@example.com" aparece en la lista de usuarios

  Scenario: No se puede crear un usuario con email repetido
    Given existe un rol con nombre "ADMIN"
    And creo un usuario con nombre "Luis", apellido "Perez", email "luis@example.com", password "password123" y rol "ADMIN"
    When intento crear otro usuario con nombre "Carlos", apellido "Mendez", email "luis@example.com", password "password456" y rol "ADMIN"
    Then ocurre un error con el mensaje "El correo electrónico ya está en uso."

  Scenario: Editar un usuario correctamente
    Given existe un rol con nombre "CLIENTE"
    And existe un usuario con nombre "Ana", apellido "Lopez", email "ana@example.com", password "password123" y rol "CLIENTE"
    When actualizo el usuario con email "ana@example.com" a nombre "Ana Maria", apellido "Lopez Perez", email "anamaria@example.com", password "newpass456" y rol "CLIENTE"
    Then el usuario con email "anamaria@example.com" tiene nombre "Ana Maria"

  Scenario: Eliminar un usuario correctamente
    Given existe un rol con nombre "CLIENTE"
    And existe un usuario con nombre "Pedro", apellido "Gomez", email "pedro@example.com", password "password123" y rol "CLIENTE"
    When elimino el usuario con email "pedro@example.com"
    Then el usuario con email "pedro@example.com" ya no existe en la base de datos

  Scenario: No se puede crear un usuario con un rol inexistente
    Given no existe ningún rol en el sistema
    When intento crear un usuario con nombre "Carlos", apellido "Diaz", email "carlos@example.com", password "1234" y rol "ADMIN"
    Then ocurre un error con el mensaje "Rol no encontrado con ID: 1"

  Scenario: No se puede editar un usuario con un email ya existente en otro usuario
    Given existe un rol con nombre "CLIENTE"
    And existe un usuario con nombre "Laura", apellido "Martinez", email "laura@example.com", password "1234" y rol "CLIENTE"
    And existe un usuario con nombre "Marta", apellido "Gonzalez", email "marta@example.com", password "5678" y rol "CLIENTE"
    When intento actualizar el usuario con email "marta@example.com" a nombre "Marta", apellido "Gonzalez", email "laura@example.com", password "91011" y rol "CLIENTE"
    Then ocurre un error con el mensaje "El correo electrónico ya está en uso por otro usuario."

  Scenario: No se puede eliminar un usuario inexistente
    When intento eliminar el usuario con email "inexistente@example.com"
    Then ocurre un error con el mensaje "Usuario no encontrado"