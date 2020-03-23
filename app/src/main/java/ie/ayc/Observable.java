package ie.ayc;

public interface Observable {
    void attach(Observer obj);
    void detach(Observer obj);
    void object_notify(UpdateSource updatesource);
    void notify_all();
}
