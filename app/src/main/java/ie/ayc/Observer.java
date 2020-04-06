package ie.ayc;

public interface Observer {
    void update(UpdateSource updatesource);
    void update(UpdateSource updatesource, UpdateResponse ur);
}
