package org.example.Suite;

import org.example.Service.AutenticacionServiceTest;
import org.example.Service.InventarioServiceTest;
import org.example.Service.ProductoServiceTest;
import org.example.Service.UsuarioServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        UsuarioServiceTest.class,
        ProductoServiceTest.class,
        InventarioServiceTest.class,
        AutenticacionServiceTest.class
})
public class RegressionTestSuite {
    // Esta clase no necesita código, solo las anotaciones
}