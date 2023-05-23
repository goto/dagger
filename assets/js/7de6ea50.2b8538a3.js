"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[2510],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>h});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function a(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?a(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):a(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},a=Object.keys(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var l=r.createContext({}),c=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},p=function(e){var t=c(e.components);return r.createElement(l.Provider,{value:t},e.children)},d="mdxType",u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},m=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,a=e.originalType,l=e.parentName,p=s(e,["components","mdxType","originalType","parentName"]),d=c(n),m=o,h=d["".concat(l,".").concat(m)]||d[m]||u[m]||a;return n?r.createElement(h,i(i({ref:t},p),{},{components:n})):r.createElement(h,i({ref:t},p))}));function h(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var a=n.length,i=new Array(a);i[0]=m;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s[d]="string"==typeof e?e:o,i[1]=s;for(var c=2;c<a;c++)i[c]=n[c];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}m.displayName="MDXCreateElement"},5025:(e,t,n)=>{n.r(t),n.d(t,{contentTitle:()=>i,default:()=>d,frontMatter:()=>a,metadata:()=>s,toc:()=>l});var r=n(7462),o=(n(7294),n(3905));const a={},i="Monitoring",s={unversionedId:"guides/monitoring",id:"guides/monitoring",isDocsHomePage:!1,title:"Monitoring",description:"For a complex application like Dagger monitoring plays an important role.",source:"@site/docs/guides/monitoring.md",sourceDirName:"guides",slug:"/guides/monitoring",permalink:"/dagger/docs/guides/monitoring",editUrl:"https://github.com/goto/dagger/edit/master/docs/docs/guides/monitoring.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Deployment",permalink:"/dagger/docs/guides/deployment"},next:{title:"Troubleshooting",permalink:"/dagger/docs/guides/troubleshooting"}},l=[{value:"Metrics Stack",id:"metrics-stack",children:[]},{value:"Dagger Dashboard",id:"dagger-dashboard",children:[]}],c={toc:l},p="wrapper";function d(e){let{components:t,...n}=e;return(0,o.kt)(p,(0,r.Z)({},c,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"monitoring"},"Monitoring"),(0,o.kt)("p",null,"For a complex application like Dagger monitoring plays an important role.\nMonitoring goes hand-in-hand with observability, which is a prerequisite for troubleshooting and performance tuning.\nThis section will give a brief overview of Dagger's monitoring stack and explain some important panels from the pre-built dashboards which might help you in running your dagger in production."),(0,o.kt)("h2",{id:"metrics-stack"},"Metrics Stack"),(0,o.kt)("p",null,"We use Flink's inbuilt metrics reporter to publish application metrics to one of the supported sinks. Other metrics like JMX can be enabled from Flink. Find more details on Flink's metrics reporting and supported sinks ",(0,o.kt)("a",{parentName:"p",href:"https://ci.apache.org/projects/flink/flink-docs-release-1.9/monitoring/metrics.html#reporter"},"here"),".\nTo register new application metrics from the dagger codebase follow ",(0,o.kt)("a",{parentName:"p",href:"https://ci.apache.org/projects/flink/flink-docs-release-1.9/monitoring/metrics.html#registering-metrics/"},"this"),"."),(0,o.kt)("p",null,"We have also included a ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/goto/dagger/blob/main/docs/static/assets/dagger-grafana-dashboard.json"},"custom grafana dashboard")," for dagger related ",(0,o.kt)("a",{parentName:"p",href:"/dagger/docs/reference/metrics"},"metrics"),".\nFollow ",(0,o.kt)("a",{parentName:"p",href:"https://grafana.com/docs/grafana/latest/dashboards/export-import/"},"this")," to import this dashboard."),(0,o.kt)("h2",{id:"dagger-dashboard"},"Dagger Dashboard"),(0,o.kt)("p",null,"This section gives an overview of the important panels/titles in the dagger ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/goto/dagger/blob/main/docs/static/assets/dagger-grafana-dashboard.json"},"dashboard"),".\nFind more about all the panels ",(0,o.kt)("a",{parentName:"p",href:"/dagger/docs/reference/metrics"},"here"),"."),(0,o.kt)("h4",{id:"overview"},"Overview"),(0,o.kt)("p",null,"It shows the basic status of a Dagger which is kind of self-explanatory like running since, number of restarts, downtime etc."),(0,o.kt)("p",null,"Listing some other important panels in this section below.\n",(0,o.kt)("inlineCode",{parentName:"p"},"Max Kafka Consumer Lag"),": shows the Consumer Lag of the Kafka consumer."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Late Records Dropped"),": The number of records a task has dropped due to arriving late."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Records Rate"),": Input - The number of records the Dagger receives per second. Output - The number of records a task sends per second."),(0,o.kt)("h4",{id:"kafka-consumer"},"Kafka Consumer"),(0,o.kt)("p",null,"All the metrics related to the Kafka Consumer. Helps you debug the issues from the consumer side. Some of the important panels here are"),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Records Consumed Rate /second")," The average number of records consumed per second."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Bytes Consumed Rate /second")," The average size of records consumed per second."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Commit Rate /second")," The average number of records committed per second. etc."),(0,o.kt)("h4",{id:"input-stream"},"Input Stream"),(0,o.kt)("p",null,"Shows some basic information about input Kafka Streams like Topics, Proto and Kafka clusters etc."),(0,o.kt)("h4",{id:"exception"},"Exception"),(0,o.kt)("p",null,"Shows the warning/errors which would be causing issues in the Dagger jobs.\n",(0,o.kt)("inlineCode",{parentName:"p"},"Fatal Exceptions")," cause the job to restart.\n",(0,o.kt)("inlineCode",{parentName:"p"},"Warnings")," are potential issues but don\u2019t restart the job."),(0,o.kt)("h4",{id:"output-stream"},"Output Stream"),(0,o.kt)("p",null,"Shows some basic information about output Kafka Stream like Topics, Proto and Kafka cluster etc. Only gets populated for Kafka sink Dagger."),(0,o.kt)("h4",{id:"udf"},"UDF"),(0,o.kt)("p",null,"Lists the Name of the UDFs in the SQL query. Also, have the Dart related metrics for a Dart Dagger."),(0,o.kt)("h4",{id:"post-processors"},"Post Processors"),(0,o.kt)("p",null,"Lists all the Post Processors in a Dagger and some of the crucial metrics related to Post Processors."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Total External Calls Rate")," total calls to the external source per minute. Which can be either one of an HTTP endpoint / ElasticSearch / PostgresDB."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Success Response Time")," Time taken by the external client to send back the response to Dagger, in case of a successful event."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Success Rate")," Number of successful events per minute."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Total Failure Request Rate")," Number of failed events per minute."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Failure Rate on XXX calls")," Number of failed events per minute due to XXX response code."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Timeouts Rate")," Number of timeouts per minute from an external source, which can be either one of an HTTP endpoint / ElasticSearch / PostgresDB, based on Post Processor type."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Close Connection On Client Rate")," Number of times connected to the external client is closed per minute."),(0,o.kt)("h4",{id:"longbow"},"Longbow"),(0,o.kt)("p",null,"Lists some important information related to the Longbow Read and Write Dagger."),(0,o.kt)("h4",{id:"checkpointing"},"Checkpointing"),(0,o.kt)("p",null,"To make the state fault-tolerant, Flink needs to checkpoint the state. Checkpoints allow Flink to recover state and positions in the streams to give the application the same semantics as a failure-free execution. A Dagger job periodically checkpoints to a GCS bucket. This section has all the checkpointing details.\n",(0,o.kt)("inlineCode",{parentName:"p"},"Number of checkpoints")," total of checkpoints triggered since the Dagger has been started."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Number of failed checkpoints")," total number of failed checkpoints since the Dagger started. If the failed checkpoint number is high, the Dagger would also restart."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Number of in-progress checkpoints")," number of checkpoints currently in progress."),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"Last checkpoint size")," is the total size of the state that needs to be checkpointed. For a larger state size (> 500 MB) one needs to increase the parallelism."))}d.isMDXComponent=!0}}]);