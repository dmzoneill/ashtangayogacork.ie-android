package ie.ayc;

/** Publishes scraper updates to attached observers. */
public interface Observable {
  /**
   * Starts delivering updates to an observer.
   *
   * @param observer listener
   */
  void attach(Observer observer);

  /**
   * Stops delivering updates to an observer.
   *
   * @param observer listener
   */
  void detach(Observer observer);

  /**
   * Notifies listeners that a source changed.
   *
   * @param updateSource changed source
   */
  void notifyObservers(UpdateSource updateSource);

  /**
   * Notifies listeners with a response payload.
   *
   * @param updateSource changed source
   * @param response payload
   */
  void notifyObservers(UpdateSource updateSource, UpdateResponse response);

  /** Notifies listeners for every source except logout. */
  void notifyEverySource();
}
