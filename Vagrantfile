# -*- mode: ruby -*-
# vi: set ft=ruby :

$LOAD_PATH << "#{File.expand_path('..',__FILE__)}/lib"
require 'travis/boxes/config'
require 'ipaddress'
require 'resolv'

require File.expand_path('../vagrant-dns/lib/vagrant-dns/configurator',__FILE__)
require File.expand_path('../vagrant-dns/lib/vagrant-dns/service',__FILE__)
Vagrant.require_plugin File.expand_path('../vagrant-dns/lib/vagrant-dns',__FILE__)
require File.expand_path('../vagrant-windows/lib/vagrant-windows',__FILE__)

network = IPAddress("192.168.67.1/24")

ENV_REGEX = /boxes\/box\-(.+)\.yml/
home2 = File.expand_path('..',__FILE__)
Dir.chdir(home2)

envs = Dir['boxes/*'].map do |dir|
    match = ENV_REGEX.match(dir)
    if (match)
        env = match[1]
        [env, Travis::Boxes::Config.new["box-#{env}"]]
    else
        nil
    end
end.compact

begin
resolv = ::Resolv::DNS.new(:nameserver_port => [['127.0.0.1',5300]],
                :search => ['dev'],
                :ndots => 1)

envs = Hash[envs]

allhosts = network.hosts
hosts = {}
envs.each_with_index do |(name, config), num|
    hosts[name] = nil
    resolv.each_address("#{name}.dev") do |addr|
        ipaddr = IPAddress(addr.address)
        #this will set hosts[name] to nil if there's a collision
        hosts[name] = allhosts.delete(ipaddr) 
    end
end
rescue
allhosts = network.hosts
hosts = {}
end

Vagrant::Config.run do |c|

  envs.each_with_index do |(name, config), num|

    c.vm.define(name) do |box|
      box.vm.box = "julia-#{config.base}"
      box.vm.host_name = config.hostname ? config.hostname : "#{name}.dev"   
      box.vm.guest = config.guest ? config.guest.to_sym : :linux
      ipaddr = hosts[name] ? hosts[name].address : allhosts.pop().address
      box.vm.network :hostonly, ipaddr, :netmask => network.netmask
      box.vm.boot_mode = :gui
      
      if box.vm.guest == :windows
        box.vm.forward_port 3389, 3390, :name => "rdp", :auto => true
        box.vm.forward_port 5985, 5985, :name => "winrm", :auto => true
        box.winrm.timeout = 1800
        box.winrm.boot_timeout = 20
      end
  
      if config.ruby_files && config.ruby_files.vm 
      config.ruby_files.vm.each do |file|
            load "#{home2}/boxes/#{file}"
      end
      end

      if config.ports? 
          config.ports[0].each_with_index do |(remote, local), i|
            box.vm.forward_port remote, local
          end
      end

      box.vm.customize [
        "modifyvm",   :id,
        "--memory",   config.memory? ? config.memory.to_s : "512",
        "--nictype1", "82540EM",
        "--vram", "64",
        "--cpus",     config.cpus? ? config.cpus : "2",
        "--ioapic",   "on"
      ]

      box.dns.tld = "dev"
      box.dns.patterns = [/^.*#{box.vm.host_name}$/, /^.*#{name}$/]

      if config.recipes? && File.directory?(config.cookbooks)
        box.vm.provision :chef_solo do |chef|
          chef.cookbooks_path = File.expand_path(config.cookbooks,home2)
          if box.vm.guest == :windows
            chef.provisioning_path = "C:/tmp"
          #  chef.binary_env = "cmd /C"
          end
          chef.log_level = :debug # config.log_level

          config.recipes.each do |recipe|
            chef.add_recipe(recipe)
          end
          
          chef.json.merge!(config.json)
          chef.json.merge!({:ipaddr=>ipaddr})

          if config.ruby_files && config.ruby_files.chef
          config.ruby_files.chef.each do |file|
            eval(File.read("#{home2}/boxes/#{file}"))
          end
          end
       end
      end
    end
  end
end
