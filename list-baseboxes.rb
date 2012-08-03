require 'travis/boxes'

ENV_REGEX = /boxes\/(box\-\w+)\.yml/

envs = Dir['boxes/*'].map do |dir|
    match = ENV_REGEX.match(dir)
    if (match)
        env = match[1]
        [env, Travis::Boxes::Config.new[env]]
    else
        nil
    end
end.compact

envs = Hash[envs]

envs.each_with_index do |(name, config), num|
 puts "julia_#{config.base}"
end
