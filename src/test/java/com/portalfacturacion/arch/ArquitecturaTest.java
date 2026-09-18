package com.portalfacturacion.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArquitecturaTest {

  private final JavaClasses classes = new ClassFileImporter().importPackages("com.portalfacturacion");

  @Test
  void domain_noDependeDeAdapters() {
    ArchRule rule = noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("..adapter..");
    rule.check(classes);
  }

  @Test
  void domain_noDependeDeFrameworks() {
    ArchRule rule = noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "org.springframework..", "jakarta.persistence..", "jakarta.servlet..",
            "io.jsonwebtoken..", "com.mysql..");
    rule.check(classes);
  }

  @Test
  void application_noDependeDeAdapters() {
    ArchRule rule = noClasses().that().resideInAPackage("..application..")
        .should().dependOnClassesThat().resideInAPackage("..adapter..");
    rule.check(classes);
  }
}
