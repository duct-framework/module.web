## 0.9.0 (2024-11-11)

* BREAKING CHANGE: removed ``:duct.module.web/site`` and `:duct.module.web/api`
* Added `:features` key to `:duct.module/web` with `:api` and `:site` options

## 0.8.1 (2024-11-01)

* Added Integrant hierarchy and annotation files

## 0.8.0 (2024-11-01)

* BREAKING CHANGE: converted library to use Integrant expansions
* Changed artifact group name
* Removed dependency on Duct core

## 0.7.4 (2024-10-25)

* Updated dependencies

## 0.7.3 (2021-06-19)

* Updated dependencies
* Added explicit jsonista dependency

## 0.7.2 (2021-03-16)

* Updated dependencies

## 0.7.1 (2020-09-04)

* Updated dependencies

## 0.7.0 (2019-01-05)

* Updated dependencies

## 0.7.0-beta1 (2018-10-29)

* Updated dependencies

## 0.7.0-alpha4 (2018-10-05)

* Changed internal server error to always return JSON (#13)

## 0.7.0-alpha3 (2018-09-28)

* Updated dependencies
* Replaced `:duct.core/handler` with `:duct.handler/root`
* Added `duct.core/resource` to static handlers

## 0.7.0-alpha2 (2018-09-22)

* Updated dependencies
* Replaced `io/resource` with `duct.core/resource`

## 0.7.0-alpha1 (2018-05-29)

* Updated module to use duct/core 0.7.0 alpha
* Fixed Muuntaja option merge

## 0.6.4 (2017-12-16)

* Updated dependencies

## 0.6.3 (2017-11-02)

* Updated dependencies

## 0.6.2 (2017-09-04)

* Fixed missing `:duct.router/cascading` derive

## 0.6.1 (2017-09-04)

* Updated `duct/core` to 0.6.1
* Updated `duct/logger` to 0.2.1

## 0.6.0 (2017-08-21)

* Updated duct/core to 0.6.0
* Removed additional requires from module for lean prep
* Updated Ring-Defaults to 0.3.1
* Updated Muuntaja to 0.3.2

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
