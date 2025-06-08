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

    org.duct-framework/module.web {:mvn/version "0.12.9"}

Or to your Leiningen project file:

    [org.duct-framework/module.web "0.12.9"]

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

[reitit]: https://github.com/metosin/reitit

You can also use the `:routes` key on the module:

```edn
{:duct.module/web
 {:features #{:site}
  :routes [["/" #ig/ref :app.example/handler]]}
 :app.example/handler {}}
```

This will automatically add appropriate refs and handler keys, so the above
configuration can be written:

```edn
{:duct.module/web
 {:features #{:site}
  :routes [["/" :app.example/handler]]}}
```

It may be the case that you want all handlers to have a common configuration,
such as providing a reference to a database. You do this easily with the
`:handler-opts` key, which merges a supplied options map with the options for
all the handlers referenced in the routes.

For example:

```edn
{:duct.database/sql {:jdbcUrl "jdbc:sqlite:"}
 :duct.module/web
 {:features #{:site}
  :handler-opts {:db #ig/ref duct.database/sql}
  :routes [["/" :app.example/handler]]}}
```

Middleware can be added in one of three ways:

- Via the top-level `:middleware` key
- Via the top-level `:route-middleware` key
- Via a `:middleware` key on a route options map

All these keys take a vector of middleware definitions:

```clojure
{:middleware
 [#ig/ref :eg/middleware1  ;; an Integrant ref
  :eg/middleware2          ;; a namespaced keyword that will be turned into a ref
  [:eg/middleware3 :arg]}  ;; middleware with an extra argument
```

Like routes, a corresponding definition will be automatically added for
each middleware reference.

The top-level `:middleware` option will add middleware to the whole
handler, regardless of whether any route matches.

The top-level `:route-middleware` option will add middleware if and only
if a route matches.

The per-route `:middleware` key will add middleware only to that route.

You can also use the `:middleware-opts` function to automatically add
common options to middleware, in the same way that `:handler-opts`
works.

## License

Copyright © 2025 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
