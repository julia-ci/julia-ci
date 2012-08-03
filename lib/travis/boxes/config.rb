require 'yaml'
require 'hashr'
require 'deep_merge/rails_compat'

module Travis
  module Boxes
    class Config
      class Definition < Hashr
        define :base      => 'natty32.box',
               :cookbooks => 'cookbooks',
               :json      => {},
               :recipes   => []
               
        attr_accessor :path       
      end

      attr_reader :definitions

      def initialize
        @definitions = {}
      end

      def definition(name)
        definitions[name.to_sym] ||= begin 
            defs = Definition.new(read(name.to_s))
            defs.path = name_to_path(name.to_s)
            defs
        end
      end
      alias :[] :definition

      def method_missing(name, *args, &block)
        args.empty? ? definition(name) : super
      end
      
      def name_to_path(name = nil)
        directory = File.expand_path('../../../../boxes', __FILE__)
        filename = [(name ? name : 'local'), 'yml'].compact.join('.')
        [directory, filename].join('/')
      end

      protected

        def read(name)
          defs = active_definition(name).deeper_merge(local[name] || {}).merge('definition' => name)
          if(defs['parent'])
            defs = read(defs['parent']).deeper_merge(defs)
          end
          defs
        end

        def local
          read_yml
        end

        def active_definition(name)
          read_yml(name)
        end

        def read_yml(name = nil)
          path = self.name_to_path(name)
          File.exists?(path) ? path : raise("Could not find a configuration file #{path}")
          YAML.load_file(path) || {}
        end

    end
  end
end

