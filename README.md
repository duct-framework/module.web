# Duct module.web

[![Build Status](https://travis-ci.org/duct-framework/module.web.svg?branch=master)](https://travis-ci.org/duct-framework/module.web)

A [Duct][] module that adds a web server and useful middleware to a
configuration. This is the basis of all web applications built with
Duct.

[duct]: https://github.com/duct-framework/duct

## Installation

To install, add the following to your project `:dependencies`:

    [duct/module.web "0.5.4"]

## Usage

To add this module to your configuration, add a reference to
`:duct.module.web/api` if you want to develop a web service:

```edn
{:duct.core/project-ns foo
 :duct.module.web/api  {}}
```

Or `:duct.module.web/site` if you want to develop a user-facing web
application:

```edn
{:duct.core/project-ns foo
 :duct.module.web/site {}}
```

Or `:duct.module/web` if you want the bare-bones approach and want to
handle most things yourself.

Note that the `:duct.core/project-ns` key must also be set, to allow
the module to find the right resources.

By default, the module uses the `:duct.server.http/jetty` key for the
webserver, as supplied by the [server.http.jetty][] library. However,
if a key deriving from `:duct.server/http` already exists in the
configuration, the module will use that instead.

Similarly, the module includes the `:duct.router/cascading` key for
routing. This is a simple router that takes an ordered vector of
handlers, and will return the first non-nil response for a given
request.

For example:

```edn
{:duct.router/cascading [#ig/ref :foo.endpoint/example1
                         #ig/ref :foo.endpoint/example2]
 :foo.endpoint/example1 {}
 :foo.endpoint/example2 {}}
```

If a key deriving from `:duct/router` exists in the configuration
already, then that is used instead.

[server.http.jetty]: https://github.com/duct-framework/server.http.jetty

## License

Copyright © 2017 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
