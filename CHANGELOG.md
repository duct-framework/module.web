## 0.5.5 (2017-07-18)

* Updated dependencies

## 0.5.4 (2017-06-24)

* Improved format middleware option merging (#3)

## 0.5.3 (2017-06-23)

* Made defaults middleware easier to selectively override

## 0.5.2 (2017-06-14)

* Fixed bug with static handlers and non-standard response bodies

## 0.5.1 (2017-06-13)

* Updated static handlers to add headers automatically for file and URL bodies
* Updated static handlers so URL bodies work with older Ring adapters

## 0.5.0 (2017-05-27)

* **BREAKING CHANGE** Replaced `duct.handler.error` with `duct.handler.static`
* Added Muuntaja for content negotiation to `:duct.module.web/api`
* Added `:duct.module/web` for bare-bones web development

## 0.4.0 (2017-05-22)

* **BREAKING CHANGE** Changed `wrap-not-found` to take handler argument
* **BREAKING CHANGE** Changed `wrap-hide-errors` to take handler argument
* Added `duct.handler.error` namespace
* Added async support to middleware functions
* Updated `duct/server.http.jetty` to support async Ring

## 0.3.0 (2017-05-07)

* **BREAKING CHANGE** `:duct.core.web/handler` key replaced by `:duct.core/handler`
* Updated module to look for `:duct/router` key for routes
* Added `:duct.router/cascading` key

## 0.2.1 (2017-05-04)

* Updated Ring to 1.6.0
* Removed `duct.server.http.jetty` require (#1)

## 0.2.0 (2017-04-24)

* **BREAKING CHANGE** Updated modules to duct/core 0.2.0 standard
* Updated Integrant to 0.4.0

## 0.1.2 (2017-04-18)

* Updated Ring to 1.6.0-RC3

## 0.1.1 (2017-04-17)

* Updated duct/server.http.jetty to 0.1.1

## 0.1.0 (2017-04-15)

* First release
