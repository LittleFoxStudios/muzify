package com.littlefoxstudios.muzify;

public class AppVersion {
    static final int MAJOR_VERSION = 1;
    static final int MINOR_VERSION = 1;

    private int availableMajorVersion;
    private int availableMinorVersion;

    public AppVersion(String version)
    {
        if(version.trim().length() == 1){
            setApp(Integer.parseInt(version.trim()), 0);
        }else{
            String[] v = version.split("\\.");
            setApp(Integer.parseInt(v[0].trim()), Integer.parseInt(v[1].trim()));
        }
    }

    void setApp(int majorV, int minorV){
        setAvailableMajorVersion(majorV);
        setAvailableMinorVersion(minorV);
    }

    public void setAvailableMajorVersion(int v){
        this.availableMajorVersion = v;
    }

    public void setAvailableMinorVersion(int v){
        this.availableMinorVersion = v;
    }

    public boolean isVersionAllowed()
    {
        return !(availableMajorVersion > MAJOR_VERSION || availableMinorVersion > MINOR_VERSION);
    }
}
