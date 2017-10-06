# EEG Toolkit for Muse Android

EEG Toolkit is an Android library for interfacing with Muse devices. It aims to simplify the Muse SDK, making it easier to connect, process and view the data streaming from a device. It is designed to work with apps using [Android Data Binding](https://developer.android.com/topic/libraries/data-binding/index.html), and contains the following:

#### [Models](https://github.com/padster/Muse-EEG-Toolkit/tree/master/eegtoolkit/src/main/java/eeg/useit/today/eegtoolkit/model) (i.e. live pure data)
* TimeSeries: A live stream of (timestamp, value) that supports history and snapshots.
* TimeSeriesSnapshot: A list of snapshot values covering a period of time.
* MergedSeries: Convert multiple single-value time series into a single multiple-value series.
* EpochCollector: Given a collection of time series, allows taking and storing a history of snapshots for them.

#### [ViewModels](https://github.com/padster/Muse-EEG-Toolkit/tree/master/eegtoolkit/src/main/java/eeg/useit/today/eegtoolkit/vm) (i.e. live data for display)
* MuseListViewModel: A live list of all Muse devices that have been detected over bluetooth.
* StreamingDeviceViewModel: A connection to a Muse device that is streaming data.
* SensorGoodViewModel: An observable set of the last isGood values for each sensor.
* ConnectionStrengthViewModel: An observable set of the last connection strength [0 - 1] values for each sensor.
* FrequencyBandViewModel: An observable set of the last frequency band values for each sensor, with many options for which band values to use.
* RawChannelViewModel: Converts a single raw channel into its timeseries.

#### [Views](https://github.com/padster/Muse-EEG-Toolkit/tree/master/eegtoolkit/src/main/java/eeg/useit/today/eegtoolkit/view)
* GraphSurfaceView: Draws a TimeSeries onto a surface view, as a scrolling line chart.
* GraphGLView: Does the as GraphSurfaceView, but onto a GLES 2.0 surface, with better performance.
* ConnectionStrengthView: Renders the live channel-wise strength onto four dots, with color indicating strength.
* Plot2DView: Given an X and Y time-series, plots their combined values on a 2D grid with a short history.
* EpochSurfaceView: Allows overlaying multiple epochs onto the same surface view.

#### [Input/Output](https://github.com/padster/Muse-EEG-Toolkit/tree/master/eegtoolkit/src/main/java/eeg/useit/today/eegtoolkit/io)
* StreamingDeviceRecorder: When turned on, creates a muse file on the device and streams live data to it.

These lists are by no means complete, and expected to be expanded upon in the future,. If you have any requirements let me know!

#### Sample App
In addition, a [sample app](https://github.com/padster/Muse-EEG-Toolkit/tree/master/sampleApp) is provided that shows example usage of the above:

![Device List](https://raw.githubusercontent.com/padster/Muse-EEG-Toolkit/master/images/deviceList.png)

Shows the list of devices found, current scanning status, and lets a user select a device.

![Device Details](https://raw.githubusercontent.com/padster/Muse-EEG-Toolkit/master/images/deviceDetails.png)

Shows the device connection status, the isGood and connection Strength for each sensor; Additionally renders channel EEG3 live to both a SurfaceView and GLSurfaceView for comparison,
and has four progress bars that render to the most recent delta/theta/alpha/beta values.

![More Device Details](https://raw.githubusercontent.com/padster/Muse-EEG-Toolkit/master/images/moreDeviceDetails.png)

Uses Plot2DView to render dots at the current and previous theta and beta score values.
Below that is a rendering of past epochs, and a way to collect more which then get drawn on the view.


#### Future plans
The following list are some ideas of what will likely be added to the Toolkit:
* View that draws a histogram, and updates its heights live as more values come in.
* Optimize the live streaming VM to reuse listeners if multiple calls try listening to the same data source (e.g. one raw value from EEG1, one raw value from EEG2, should reuse the EEG listener).
* Clean up all the listenable objects.
* Fix up lifecycle, unlistening to things no longer needed.
* Add more customization options for each of the views. This are intentionally omitted, but should be easy to add on an on-needed basis, just let me know!

The list is tentative however, and may change. If you have any suggestions, file an issue here. Pull requests also welcome to add anything that seems useful :)
