package ie.ayc;

public interface Observable {
    void attach(Observer obj);
    void detach(Observer obj);
    void object_notify(UpdateSource updatesource);
    void object_notify(UpdateSource updatesource, UpdateResponse ur);
    void notify_all();
}
