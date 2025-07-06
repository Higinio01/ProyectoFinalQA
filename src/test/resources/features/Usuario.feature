Feature: Gestion de usuarios en el sistema

  Scenario: Crear un usuario correctamente
    Given existe un rol con nombre "CLIENTE"
    When creo un usuario con nombre "Sara", apellido "Contreras", email "sara@example.com", password "password123" y rol "CLIENTE"
    Then el usuario con email "sara@example.com" aparece en la lista de usuarios

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

  Scenario: No se puede eliminar un usuario inexistente
    When intento eliminar el usuario con email "inexistente@example.com"
    Then ocurre un error con el mensaje "Usuario no encontrado"