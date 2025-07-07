Feature: Gestión de usuarios en el sistema

  Scenario: Crear un usuario correctamente
    Given existe un rol con nombre "CLIENTE"
    When creo un usuario con nombre "Sara", apellido "Contreras", email "sara@example.com", password "password123" y rol "CLIENTE"
    Then el usuario con email "sara@example.com" aparece en la lista de usuarios

  Scenario: Editar un usuario correctamente
    Given existe un rol con nombre "CLIENTE"
    And existe un usuario con nombre "Ana", apellido "Lopez", email "ana@example.com", password "password123" y rol "CLIENTE"
    When actualizo el usuario con email "ana@example.com" a nombre "Ana Maria", apellido "Lopez Perez", email "anamaria@example.com", password "newpass456" y rol "CLIENTE"
    Then el usuario con email "anamaria@example.com" tiene nombre "Ana Maria"

  Scenario: Eliminar un usuario correctamente por email
    Given existe un rol con nombre "CLIENTE"
    And existe un usuario con nombre "Pedro", apellido "Gomez", email "pedro@example.com", password "password123" y rol "CLIENTE"
    When elimino el usuario con email "pedro@example.com"
    Then el usuario con email "pedro@example.com" ya no existe en la base de datos

  Scenario: No se puede eliminar un usuario inexistente por email
    When intento eliminar el usuario con email "inexistente@example.com"
    Then ocurre un error con el mensaje "Usuario no encontrado"

  Scenario: Bloquear un usuario correctamente
    Given existe un rol con nombre "CLIENTE"
    And existe un usuario con nombre "Carla", apellido "Diaz", email "carla@example.com", password "password123" y rol "CLIENTE"
    When bloqueo el usuario con email "carla@example.com"
    Then el usuario con email "carla@example.com" tiene estado "BLOQUEADO"

  Scenario: No se puede crear un usuario con email repetido
    Given existe un rol con nombre "CLIENTE"
    And existe un usuario con nombre "Lucas", apellido "Martinez", email "lucas@example.com", password "password123" y rol "CLIENTE"
    When intento crear un usuario con nombre "Lucas", apellido "Martinez", email "lucas@example.com", password "password123" y rol "CLIENTE"
    Then ocurre un error con el mensaje "ya está en uso"

  Scenario: No se puede crear un usuario sin datos requeridos
    Given existe un rol con nombre "CLIENTE"
    When intento crear un usuario con nombre "", apellido "Gomez", email "", password "password123" y rol "CLIENTE"
    Then ocurre un error con el mensaje "Todos los campos son obligatorios"

  Scenario: Crear un usuario con rol ADMIN
    Given existe un rol con nombre "ADMIN"
    When creo un usuario con nombre "Jose", apellido "Ramirez", email "jose@example.com", password "adminpass", rol "ADMIN"
    Then el usuario con email "jose@example.com" tiene rol "ADMIN"

  Scenario: No se puede crear un usuario con un rol inexistente por nombre
    When intento crear un usuario con nombre "Laura", apellido "Perez", email "laura@example.com", password "password123" y rol "INEXISTENTE"
    Then ocurre un error con el mensaje "Rol no encontrado"

  Scenario: No se puede crear un usuario con email ya registrado usando ID
    Given existe un rol con ID 1
    And existe un usuario con nombre "Mario", apellido "Lopez", email "repetido@example.com", password "password123" y rol ID 1
    When intento crear un usuario con nombre "Repetido", apellido "Perez", email "repetido@example.com", password "pass123" y rol ID 1
    Then ocurre un error con el mensaje "El correo electrónico ya está en uso: repetido@example.com"

  Scenario: No se puede actualizar un usuario con un email duplicado usando ID
    Given existe un rol con ID 1
    And existe un usuario con ID 1 y email "original@example.com"
    And existe otro usuario con ID 2 y email "usado@example.com"
    When intento actualizar el usuario con ID 1 a email "usado@example.com"
    Then ocurre un error con el mensaje "El correo electrónico ya está en uso por otro usuario: usado@example.com"
