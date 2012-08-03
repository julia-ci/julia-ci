module JBox
class JBoxCloud < Java.hudson.slaves.Cloud
    include Jenkins::Model
    include Jenkins::Model::Describable
    describe_as Java.hudson.slaves.Cloud
    
    def initialize(native)
        super(native)
    end
    

    
    def getDisplayName
        return "JBox Cloud"
    end
    
    def canProvision 
        false
    end
    
    def all
        
    end
end
end
