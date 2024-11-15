# Duct module.web [![Build Status](https://github.com/duct-framework/module.web/actions/workflows/test.yml/badge.svg)](https://github.com/duct-framework/module.web/actions/workflows/test.yml)

A [Duct][] module that adds a web server and useful middleware to a
configuration. This is the basis of all web applications built with
Duct.

This current version is experimental and will only work with the new
[duct.main][] tool. The artifact group name has been changed to prevent
accidental upgrades. The version prior to this change was: `0.7.4`.

[duct]: https://github.com/duct-framework/duct
[duct.main]: https://github.com/duct-framework/duct.main

## Installation

Add the following dependency to your deps.edn file:

    org.duct-framework/module.web {:mvn/version "0.9.0"}

Or to your Leiningen project file:

    [org.duct-framework/module.web "0.9.0"]

## Usage

To add this module to your configuration, add a reference to
`:duct.module/web`:

```edn
{:duct.module/web {}}
```

To load in middleware and handlers appropriate to develop web services,
add the `:api` keyword to the `:features` option:

```edn
{:duct.module/web {:features #{:api}}}
```

Or `:site` if you want to develop a user-facing web application:

```edn
{:duct.module/web {:features #{:site}}}
```

Or include both if you want both sets of features combined.

By default, the module uses the [Reitit][] Ring router. This is
available via the `:duct.router/reitit` key. For example:

```edn
{:duct.module/web {:features #{:site}}
 :duct.router/reitit {:routes [["/" #ig/ref :app.example/handler]]}}
 :app.example/handler {}
```

You can also use the `:routes` key on the module:

```edn
{:duct.module/web
 {:features #{:site}
  :routes [["/" #ig/ref :app.example/handler]]}
 :app.example/handler {}}
```

[reitit][]: https://github.com/metosin/reitit

## License

Copyright © 2024 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
