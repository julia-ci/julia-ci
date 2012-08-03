require 'travis/boxes/config'

module JBox
    CONF_REGEX = /boxes\/(.+)\.yml/
    BOX_REGEX = /box\-(.+)/
    WORKER_REGEX = /box-worker-(.+)/
    def self.home
        @home ||= Pathname.new(File.expand_path('../../../',__FILE__))
    end
    
    def self.config
        @cofig || begin
            Dir.chdir(home) do
                envs = Dir['boxes/*'].map do |dir|
                    match = CONF_REGEX.match(dir)
                    if (match)
                        env = match[1]
                        [env, Travis::Boxes::Config.new[env]]
                    else
                        nil
                    end
                end.compact
                return @config = Hash[envs]
            end
        end
    end
    
    def self.vagrant
        @vagrant || begin 
            $stdout.sync = true
            $stderr.sync = true
            @vagrant = Vagrant::Environment.new(:cwd => home,:ui_class => Vagrant::UI::Colored) #Must be basic to support non-tty streams
            return @vagrant
        end
    end
    
    def self.find_base(box_name)
        base = ""
        JBox::config.each_with_index do |(k,config), num|
            match = BOX_REGEX.match(k)
            if(match&&match[1]==box_name)
                base=config['base']
                break
            end
        end
        if base == ""
            #env.logger.fatal("Box '#{box_name}' was not found")
            raise Veewee::Error, "Box '#{box_name}' was not found"
        end
        base
    end
end

module JBox 
    module Command
        class VirtualBox < ::Veewee::Command::GroupBase
        
            register "jbox", "Subcommand for julia specific veewee/vagrant tasks"
            desc "list", "Lists boxes"
            
            def list
                JBox::config.each_with_index do |(k,config), num|
                    match = BOX_REGEX.match(k)
                    if(match)
                        puts "#{match[1]} in #{config.path}"
                    end
                end
            end
            
            desc "listbase", "List all used base boxes"
            
            def listbase
                boxes = {}
                JBox::config.each_with_index do |(k,config), num|
                    match = BOX_REGEX.match(k)
                    if(match)
                        if(boxes[config.base])
                            boxes[config.base].push match[1]
                        else
                            boxes[config.base] = [match[1]]
                        end
                    end
                end
                boxes.each_with_index do |(base,used)|
                    puts base
                    used.each do |box|
                        puts "  #{box}"
                    end
                end
            end
            
            desc "prepare", "Create the definition and prepare the basebox for building"
            method_option :force,:type => :boolean , :default => false, :aliases => "-f", :desc => "overwrite the definition"
            def prepare(box_name)
                define_box(JBox.find_base(box_name),options)
            end
            
            desc "buildbase", "Build the basebox"
            def buildbase(box_name)
                build_base(box_name,options)
            end
            
            desc "rebuildbase", "Force rebuild of basebox"
            def rebuildbase(box_name)
                build_base(box_name,options.merge({:force => true}))
            end
  
            method_option :force,:type => :boolean , :default => false, :aliases => "-f", :desc => "overwrite the option"           
            desc "exportbase", "Export basebox"
            def exportbase(box_name)
                base = JBox.find_base(box_name)
                definition_name = "julia-#{base}"
                export_box(definition_name,options)
            end
            
            protected
            
            def define_box(base,options)
                Dir.chdir("#{JBox::home}/baseboxes") 
                options = {}
                venv=Veewee::Environment.new(options)
                venv.ui=env.ui
                venv.definitions.define("julia-#{base}",base,options)
            end
            
            def build_base(box_name, options)
                base = JBox.find_base(box_name)
                definition_name = "julia-#{base}"
                Dir.chdir("#{JBox::home}/baseboxes") 
                if(!File.directory? "definitions/#{definition_name}")
                    define_box(base,options)
                end
                # Build
                buildopts = options.merge({:auto => true, :nogui => false});
                venv=Veewee::Environment.new(buildopts)
                venv.ui=env.ui
                venv.providers["virtualbox"].get_box(definition_name).build(buildopts)
                # Export
                export_box(definition_name,options)
            end
            
            def export_box(definition_name,options)
                Dir.chdir("#{JBox::home}/baseboxes") 
                if File.exists?("#{definition_name}.box") && options[:force]
                    File.delete "#{definition_name}.box"
                end
                begin
                    opts = {"include" => [], "vagrantfile" => []}
                    venv=Veewee::Environment.new(opts)
                    venv.ui=@env.ui
                    venv.providers["virtualbox"].get_box(definition_name).export_vagrant(opts)
                rescue Veewee::Error => ex
                    venv.ui.error(ex,:prefix => false)
                    exit -1
                end
                venv.ui.info "Adding base box to vagrant:"
                if !options[:force]
                ::JBox.vagrant.cli("box","add","#{definition_name}","./#{definition_name}.box")
                else
                ::JBox.vagrant.cli("box","add","#{definition_name}","./#{definition_name}.box","--force")
                end
            end
        end
    end
end

module JBox
    module Command
        class WorkerCommand < ::Veewee::Command::GroupBase
            register "worker", "Worker related commands"
            desc "list", "Lists all available workers"
            
            def list
                workers = []
                JBox::config.each_with_index do |(k, config), num|
                    match = WORKER_REGEX.match(k)
                    if(match)
                        workers.push(match[1])
                    end
                end
                workers.each do |worker|
                    puts worker
                end
            end
            
            desc "up", "Start a worker with the given name"
            def up(box_name)
                Dir.chdir(::JBox.home)
                ::JBox.vagrant.cli("up","box-worker-"+box_name)
            end
            
            desc "status", "Vagrant Status"
            def status()
                ::JBox.vagrant.cli("status")
            end
        end
    end
end
