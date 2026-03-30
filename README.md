# Jena ExecTracker

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.aksw.jena.exectracker/jena-exectracker-parent)](https://mvnrepository.com/artifact/org.aksw.jena.exectracker)

SPARQL Execution Tracking for Apache Jena via interception at `QueryEngineRegistry` and `UpdateEngineRegistry`.

## Fuseki Plugin

<table>
  <tr>
    <td align="center">
      <img src="docs/images/fuseki-exectracker-services.png" alt="Fuseki ExecTracker Services" style="max-height: 300px; height: auto;" />
      <br><em>Services</em>
    </td>
    <td align="center">
      <img src="docs/images/fuseki-exectracker-dashboard.png" alt="Fuseki ExecTracker Dashboard" style="max-height: 300px; height: auto;" />
      <br><em>Dashboard</em>
    </td>
  </tr>
</table>

The Fuseki Plugin is published with releases: [Latest Release](/releases/latest).

A complete example setup is provided in [example-setup-fuski](example-setup-fuseki).

The following is an example for setting up exec tracker endpoints with a Fuseki service.

```turtle
# <fuseki-root>/run/configuration/example-exectracker_ds.ttl
PREFIX fuseki:    <http://jena.apache.org/fuseki#>
PREFIX ja:        <http://jena.hpl.hp.com/2005/11/Assembler#>

PREFIX jetf:      <https://w3id.org/aksw/jena/exectracker/fuseki#>

<#service> a fuseki:Service ;
  fuseki:name "example-exectracker" ;
  fuseki:dataset <#baseDS> ;
  fuseki:endpoint [
    fuseki:operation fuseki:query ;
  ] ;
  fuseki:endpoint [
    fuseki:name "update" ;
    fuseki:operation fuseki:update ;
    fuseki:allowedUsers "test" ;
  ] ;
  fuseki:endpoint [
    fuseki:operation fuseki:gsp_rw ; 
    fuseki:name "data" ;
    fuseki:allowedUsers "test" ;
  ] ;

  # Public endpoint where users can view − but not abort − SPARQL executions:
  fuseki:endpoint [
    fuseki:name "tracker" ;
    fuseki:operation jetf:exectracker ;
    ja:context [ ] ; # !!! Required to prevent start-up failure due to null context !!!
  ] ;

  # Access-protected endpoint where authorized users can view and abort excutions:
  fuseki:endpoint [
    fuseki:name "admin-tracker" ;
    fuseki:operation jetf:exectracker ;
    ja:context [
      # Jena 6.1.0+ allows `ja:cxtName jetf:allowAbort`
      ja:cxtName "https://w3id.org/aksw/jena/exectracker/fuseki#allowAbort" ;
      ja:cxtValue true ;
    ] ;
    fuseki:allowedUsers "test" ;
  ] ;
  .

<#baseDS>
  a ja:RDFDataset ;
  .
```

Terminology note: We use `fuseki-mod` to refer to the development artifact. The `fuseki-plugin` is the *packaging* of the `fueski-mod` as a drop-in JAR file.


### Build from Source

For convenience, a self-describing `Makefile` is provided:

```bash
$ make
make help                # Show these help instructions
make fuseki-plugin       # Create the self-contained ExecTracker Fuseki Plugin JAR
make release-github      # Create files for Github upload
```

```
$ make fuseki-plugin

# (Maven build output)

Created package:

/.../jena-exectracker-parent/jena-exectracker-pkg-fuseki-plugin/target/jena-exectracker-fuseki-plugin-0.1.0-SNAPSHOT.jar
```

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please read our [contributing guidelines](CONTRIBUTING.md) before opening a pull request.

## Contact

* Issue tracker: https://github.com/Scaseco/graphql4sparql/issues
* Author: [Claus Stadler](http://aksw.org/ClausStadler)

