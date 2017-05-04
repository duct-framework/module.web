# Duct module.web

[![Build Status](https://travis-ci.org/duct-framework/module.web.svg?branch=master)](https://travis-ci.org/duct-framework/module.web)

A [Duct][] module that adds a web server and useful middleware to a
configuration. This is the basis of all web applications built with
Duct.

[duct]: https://github.com/duct-framework/duct
[logger.timbre]: https://github.com/duct-framework/logger.timbre

## Installation

To install, add the following to your project `:dependencies`:

    [duct/module.web "0.2.1"]

## Usage

To add this module to your configuration, add a reference to
`:duct.module.web/api` if you want to develop a web service:

```edn
{:duct.core/project-ns foo
 :duct.core/modules    [#ref :duct.module.web/api]
 :duct.module.web/api  {}}
```

Or `:duct.module.web/site` if you want to develop a user-facing web
application:

```edn
{:duct.core/project-ns foo
 :duct.core/modules    [#ref :duct.module.web/site]
 :duct.module.web/site {}}
```

Note that the `:duct.core/project-ns` key must also be set, to allow
the module to find the right resources.

## License

Copyright © 2017 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
