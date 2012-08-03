require 'travis/boxes'

ENV_REGEX = /boxes\/(box\-\w+)\.yml/
envs={}
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

envs
