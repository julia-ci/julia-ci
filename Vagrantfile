# -*- mode: ruby -*-
# vi: set ft=ruby :

$: << 'lib'
require 'travis/boxes'

ENV_REGEX = /config\/worker\.(\w+)\.yml/

envs = Dir['config/*'].map do |dir|
    match = ENV_REGEX.match(dir)

    if (match && match[1] != 'base')
        env = match[1]
        [env, Travis::Box::Config.new[env]]
    else
        nil
    end
end.compact

Vagrant::Config.run do |config|
  envs.each_with_index do |(name, config), num|

    full_name = "travis-#{name}"

    c.vm.define(full_name) do |box|
      box.vm.box = full_name
      box.vm.forward_port(22, 3340 + num, :name => "ssh")

      box.vm.customize [
        "modifyvm",   :id,
        "--memory",   config.memory.to_s,
        "--name",     "#{full_name}-base",
        "--nictype1", "Am79C973",
        "--cpus",     "2",
        "--ioapic",   "on"
      ]

      if config.recipes? && File.directory?(config.cookbooks)
        box.vm.provision :chef_solo do |chef|
          chef.cookbooks_path = config.cookbooks
          chef.log_level = :debug # config.log_level

          config.recipes.each do |recipe|
            chef.add_recipe(recipe)
          end

          chef.json.merge!(config.json)
        end
      end
    end
  end
  config.vm.define :chefs do |chefs_config|
    chefs_config.vm.box = "ubuntu11.10"
    chefs_config.vm.provision :chef_solo do |chef|
      chef.cookbooks_path = "cookbooks"
      chef.node_name="chefserver" 
      chef.run_list.clear
      chef.add_recipe("apt::default")
      chef.add_recipe("build-essential")
      chef.add_recipe("chef-server::rubygems-install") 
      chef.json={
        :chef_server=>{
        :server_url=> "http://localhost.localdomain:4000",
        :webui_enabled=> true,
      }
    }
    end
    chefs_config.vm.forward_port 4000, 4000
    chefs_config.vm.forward_port 4040, 4040
    chefs_config.vm.forward_port 22, 2223
    chefs_config.vm.network :hostonly, "192.168.50.3"
  end
  config.vm.define :jenkins do |jenkins_config|
    jenkins_config.vm.box = "ubuntu11.10"
    jenkins_config.vm.provision :chef_solo do |chef|
      chef.cookbooks_path = "cookbooks"
      chef.node_name="jenkins" 
      chef.run_list.clear
      chef.add_recipe("apt::default")
      chef.add_recipe("java")
      chef.add_recipe("git")
      chef.add_recipe("julia-jenkins")
      chef.json = {
          :julia_jenkins => {
              :github => {
                  :clientID => IO.read(".github.clientID")[0..-2],
                  :clientSecret => IO.read(".github.clientSecret")[0..-2],
                  :organization => "JuliaLang",
                  :admin_user => "loladiro"
              }
          },
          :jenkins => {
              :node => {
                  :name => "testworker",
                  :executors => 1,
                  :ssh_user => "vagrant",
                  :ssh_pass => "vagrant",
                  :ssh_host => "testworker",
                  :availability => "demand",
              }
          }
      }
    end
    jenkins_config.vm.host_name = "jenkins"
    jenkins_config.vm.forward_port 8080, 8080
    jenkins_config.vm.forward_port 22, 2224 
    jenkins_config.vm.network :hostonly, "192.168.50.4"
  end
  config.vm.define :test_worker do |worker_config|
    worker_config.vm.box = "ubuntu11.10"
    worker_config.vm.provision :chef_solo do |chef|
        chef.cookbooks_path = "cookbooks"
        chef.node_name = "testworker"
        chef.run_list.clear
        chef.add_recipe("build-essential")
        chef.add_recipe("git")
        chef.add_recipe("java") 
        chef.add_recipe("gfortran")
  end
  worker_config.vm.host_name = "testworker"
  worker_config.vm.forward_port 22, 2225      
  worker_config.vm.network :hostonly, "192.168.50.10" 
  end
end
