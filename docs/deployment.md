# Deployment

The app is prepared for deployment on either Heroku or Dokku on Digital Ocean.
Both hosting options have been tested and proven to work with current master.
Configuration is pretty much trivial to do by following official platform documentation.
Deployment can be done with a simple git push.

## Heroku
pros:

-  more integrated solution
- easier setup
- probably better support
- better marketplace

cons:

- 512mb RAM on cheapest dynos is easily exceeded even without traffic
- frequent failures on startup because of 60s timeout (can be exceeded to 120s by support request)


### Pricing

dyno:
$50/mo - 1 GB

managed postgres:
free   - 10k rows
$9/mo  - 10^7 rows
$50/mo - 64 GB
https://www.heroku.com/pricing
https://elements.heroku.com/addons/heroku-postgresql

### buildpacks
```
heroku buildpacks:clear
heroku buildpacks:add heroku/nodejs
heroku buildpacks:add heroku/clojure
```

## Dokku on Digital Ocean
http://dokku.viewdocs.io/dokku~v0.17.9/getting-started/install/digitalocean/

pros:

- cheaper

cons:

- less supported (?)



### Pricing

droplet:
$10/mo -  2 GB / 1 CPU - 50 GB SSD disk  - 2 TB transfer

managed postgres:
free - hosted on droplet via dokku postgres plugin
$15/mo - 1 GB RAM / 1 vCPU / 10 GB Disk

https://www.heroku.com/pricing
https://elements.heroku.com/addons/heroku-postgresql


### buildpacks
```
dokku buildpacks:clear syncrate-fulcro
dokku buildpacks:add syncrate-fulcro https://github.com/heroku/heroku-buildpack-nodejs
dokku buildpacks:add syncrate-fulcro https://github.com/heroku/heroku-buildpack-clojure
```
