# Kinesis sample application - Web Analytics

Kinesis is a new service recently launched by AWS for real-time
processing of streaming big-data.

This application shows how to use Kinesis to build a very simple
real-time web analytics platform. It has two main components: the
**collector** and the **dashboard**.

## The collector

The collector is a web application that exposes a single path: `/collect`.

Every hit on this path results in a PUT request, injecting data (records)
into Kinesis. Each record contains a few pieces of data about the hit
being tracked: the Source IP (a best-effort match), the User-Agent, the
Referer, a Session ID and a Timestamp.

This Session ID is stored as a cookie in the browser that made the hit. This
allows a sequence of hits from the same session.


## The dashboard

The dashboard is another web application that consumes the Kinesis stream
described above and creates real-time visualizations of the data.

In the current version, it simply shows line graphs with the number of page views
aggregated in three different ways:

 - per second (for the last minute),
 - per minute (for the last hour), and
 - per hour (for the last day)

By accessing `/dashboard` on this application, some JavaScript code will
interact with the server through AJAX calls, pulling data from three other
endpoints: `/api/minute`, `/api/hour`, `/api/day`.

To keep the focus on Kinesis and keep the code as simple as possible,
the Dashboard application doesn't persist data. It makes all calculations
in-memory.


# Usage

## Tracking tag

Let's suppose you want to track the visits on a website, for example,
`www.tracked.com`. Let's also suppose you already deployed the two
components of this sample application on `collector.example.com` and
`dashboard.example.com`.

To initiate tracking pageviews, you have to insert a tag on each page
served by `www.tracked.com`:

    <script src="http://collector.example.com/collect"></script>

This will hit the URL described above and make a record be injected into
the Kinesis stream.

## Visualizing data

In less than 10 seconds after inserting the tracking tag on the
tracked web site, there will be records available for consumption
by the Dashboard application.

By pointing your browser to `http://dashboard.example.com/dashboard`,
you should start seeing realtime pageviews on the tracked website.



# Deployment & Costs

## Creating a Kinesis Stream

In order to deploy this sample application, you'll need first to
create a Kinesis stream. To do so, point your browser to
`https://console.aws.amazon.com/kinesis`, and create a Stream
called `hits` with 1 shard. If you wish to use another Stream name,
you'd have to modify the file modules/common/conf/application.conf

## Installing Play Framework 2.2.1

This application is built on the Play Framework 2.2.1. Please visit
`http://playframework.com` and follow the installation instructions
for your operating system.

## Building the WAR packages

Once you have the Play Framework 2.2.1 up and running, `cd` into
the project root directory (ie, the one with this file) and run the
command:

    play war

It should take less than a minute to build the two .WAR files:

 - modules/collector/target/collector-0.1-DATE-TIME.war
 - modules/dashboard/target/dashboard-0.1-DATE-TIME.war

## Creating Elastic Beanstalk applications

You'll need to create two Elastic Beanstalk applications:
one for the collector and one for the dashboard.

Point your browser to `https://console.aws.amazon.com/elasticbeanstalk`
and create two new Tomcat 7 Java 7 applications.

## Elastic Beanstalk environment configuration

Since this is a multi-subproject Play project, you'll need to
specify two parameters to the JVM on Elastic Beanstalk.

Go to the configuration tab and add the following JVM options:

 - collector application: `-Dconfig.resource="collector-application.conf"`
 - dashboard application: `-Dconfig.resource="dashboard-application.conf"`



## Test it!

Make a few pageviews on the tracked website (or, alternatively, point
your browser to the `/collect` endpoint on the collector application
and hit refresh a few times).

Then go to the Dashboard endpoint (i.e., `/dashboard` on the dashboard
application) and see the pageviews appearing in real-time!
