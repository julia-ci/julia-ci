require 'archive/tar/minitar'
require 'json'
require 'travis/boxes'
require 'set'

module Travis
  module Boxes
    module Cli
      class Veewee < Thor
        namespace "travis:base"

        include Cli

        desc 'buildall', 'Build all base boxes'

        def buildall 
            boxes = Set.new
            envs.each_with_index do |(name, config), num|
                boxes.add "julia_#{config.base}"
            end
            boxes.each do |box|
                start_build(box) 
            end
        end

        desc 'listall', 'List all base boxes'
        def listall
            envs.each_with_index do |(name, config), num|
                puts "julia_#{config.base}"
            end
        end

        desc 'build', 'Build a base box from a veewee definition(eg. oneiric32.box)'
        method_option :definition, :aliases => '-d', :default => 'oneiric32', :desc => 'Definition to build the base box from (e.g. oneiric32)'
        method_option :upload,     :aliases => '-u', :desc => 'Upload the box'

        def build
            start_build definition 
        end

        desc 'define', 'Wrapper for veewee define'
        method_option :custom, :desc => "Custom alias"
        method_option :definition, :desc => "The definition template"

        def define
            veewee define custom definition 
        end

        desc 'upload', 'Upload a base box'
        method_option :definition, :aliases => '-d', :default => 'oneiric32', :desc => 'Definition of the box to upload (e.g. oneiric32)'

        def upload
          remote = ::Travis::Boxes::Remote.new
          remote.upload("boxes/#{definition}.box", "bases/#{definition}.box")
        end

        protected

          def start_build(definition)
          Dir.chdir(File.expand_path('../../../../../baseboxes', __FILE__)) do
            run <<-sh
            veewee vbox build '#{definition} --force'
            vagrant basebox export #{definition}
            mv #{definition}.box build/#{definition}.box
            sh
          end
          end

          def veewee_define(custom,definition)
          Dir.chdir(File.expand_path('../../../../../baseboxes', __FILE__)) do
            run <<-sh
            veewee vbox define '#{custom}' '#{definition}'
            sh
          end
          end

          ENV_REGEX = /boxes\/(box\-\w+)\.yml/
          def envs
            envs = []
            Dir.chdir(File.expand_path('../../../../..', __FILE__)) do
                envs = Dir['boxes/*'].map do |dir|
                    match = ENV_REGEX.match(dir)
                    if (match)
                        env = match[1]
                        [env, Travis::Boxes::Config.new[env]]
                    else
                        nil
                    end
                end.compact
		    end

		    Hash[envs]
          end

          def vbox
            @vbox ||= Vbox.new('', options)
          end

          def config
            @config ||= ::Travis::Boxes::Config.new
          end

          def definition
            options['definition']
          end
      end
    end
  end
end
