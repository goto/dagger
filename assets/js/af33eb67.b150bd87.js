"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[7026],{3905:(e,r,t)=>{t.d(r,{Zo:()=>l,kt:()=>f});var n=t(7294);function a(e,r,t){return r in e?Object.defineProperty(e,r,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[r]=t,e}function o(e,r){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);r&&(n=n.filter((function(r){return Object.getOwnPropertyDescriptor(e,r).enumerable}))),t.push.apply(t,n)}return t}function s(e){for(var r=1;r<arguments.length;r++){var t=null!=arguments[r]?arguments[r]:{};r%2?o(Object(t),!0).forEach((function(r){a(e,r,t[r])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(r){Object.defineProperty(e,r,Object.getOwnPropertyDescriptor(t,r))}))}return e}function i(e,r){if(null==e)return{};var t,n,a=function(e,r){if(null==e)return{};var t,n,a={},o=Object.keys(e);for(n=0;n<o.length;n++)t=o[n],r.indexOf(t)>=0||(a[t]=e[t]);return a}(e,r);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(n=0;n<o.length;n++)t=o[n],r.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(a[t]=e[t])}return a}var c=n.createContext({}),u=function(e){var r=n.useContext(c),t=r;return e&&(t="function"==typeof e?e(r):s(s({},r),e)),t},l=function(e){var r=u(e.components);return n.createElement(c.Provider,{value:r},e.children)},p={inlineCode:"code",wrapper:function(e){var r=e.children;return n.createElement(n.Fragment,{},r)}},d=n.forwardRef((function(e,r){var t=e.components,a=e.mdxType,o=e.originalType,c=e.parentName,l=i(e,["components","mdxType","originalType","parentName"]),d=u(t),f=a,g=d["".concat(c,".").concat(f)]||d[f]||p[f]||o;return t?n.createElement(g,s(s({ref:r},l),{},{components:t})):n.createElement(g,s({ref:r},l))}));function f(e,r){var t=arguments,a=r&&r.mdxType;if("string"==typeof e||a){var o=t.length,s=new Array(o);s[0]=d;var i={};for(var c in r)hasOwnProperty.call(r,c)&&(i[c]=r[c]);i.originalType=e,i.mdxType="string"==typeof e?e:a,s[1]=i;for(var u=2;u<o;u++)s[u]=t[u];return n.createElement.apply(null,s)}return n.createElement.apply(null,t)}d.displayName="MDXCreateElement"},113:(e,r,t)=>{t.r(r),t.d(r,{contentTitle:()=>s,default:()=>l,frontMatter:()=>o,metadata:()=>i,toc:()=>c});var n=t(7462),a=(t(7294),t(3905));const o={},s="Choosing a Source",i={unversionedId:"guides/choose_source",id:"guides/choose_source",isDocsHomePage:!1,title:"Choosing a Source",description:"Dagger currently supports 3 kinds of data sources:",source:"@site/docs/guides/choose_source.md",sourceDirName:"guides",slug:"/guides/choose_source",permalink:"/dagger/docs/guides/choose_source",editUrl:"https://github.com/odpf/dagger/edit/master/docs/docs/guides/choose_source.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Overview",permalink:"/dagger/docs/guides/overview"},next:{title:"Create a job",permalink:"/dagger/docs/guides/create_dagger"}},c=[{value:"<code>KAFKA_SOURCE</code> and <code>KAFKA_CONSUMER</code>",id:"kafka_source-and-kafka_consumer",children:[]},{value:"<code>PARQUET_SOURCE</code>",id:"parquet_source",children:[]}],u={toc:c};function l(e){let{components:r,...t}=e;return(0,a.kt)("wrapper",(0,n.Z)({},u,t,{components:r,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"choosing-a-source"},"Choosing a Source"),(0,a.kt)("p",null,"Dagger currently supports 3 kinds of data sources:"),(0,a.kt)("h2",{id:"kafka_source-and-kafka_consumer"},(0,a.kt)("inlineCode",{parentName:"h2"},"KAFKA_SOURCE")," and ",(0,a.kt)("inlineCode",{parentName:"h2"},"KAFKA_CONSUMER")),(0,a.kt)("p",null,"Both these sources use ",(0,a.kt)("a",{parentName:"p",href:"https://kafka.apache.org/"},"Kafka")," as the source of data. ",(0,a.kt)("inlineCode",{parentName:"p"},"KAFKA_SOURCE")," uses Flink's ",(0,a.kt)("a",{parentName:"p",href:"https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/connectors/datastream/kafka/#kafka-source"},"Kafka Source")," streaming\nconnector, built using Flink's Data Source API. ",(0,a.kt)("inlineCode",{parentName:"p"},"KAFKA_CONSUMER")," is based on the now deprecated Flink\n",(0,a.kt)("a",{parentName:"p",href:"https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/connectors/kafka.html#kafka-consumer"},"Kafka Consumer"),"\nstreaming connector."),(0,a.kt)("p",null,"They are used for unbounded data streaming use cases, that is, for operating on data generated in real time. In order to\nconfigure any of these, you would need to set up Kafka(1.0+) either in a local or clustered environment. Follow this\n",(0,a.kt)("a",{parentName:"p",href:"https://kafka.apache.org/quickstart"},"quick start")," to set up Kafka in the local machine. If you have a clustered Kafka\nyou can configure it to use in Dagger directly."),(0,a.kt)("h2",{id:"parquet_source"},(0,a.kt)("inlineCode",{parentName:"h2"},"PARQUET_SOURCE")),(0,a.kt)("p",null,"This source uses Parquet files as the source of data. It is useful for bounded data streaming use cases, that is,\nfor processing data as a stream from historically generated Parquet Files of fixed size. The parquet files can be either hourly\npartitioned, such as"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-text"},"root_folder\n    - booking_log\n        - dt=2022-02-05\n            - hr=09\n                * g6agdasgd6asdgvadhsaasd829ajs.parquet\n                * . . . (more parquet files)\n            - (...more hour folders)\n        - (... more date folders)\n\n")),(0,a.kt)("p",null,"or date partitioned, such as:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-text"},"root_folder\n    - shipping_log\n        - dt=2021-01-11\n            * hs7hasd6t63eg7wbs8swssdasdasdasda.parquet\n            * ...(more parquet files)\n        - (... more date folders)\n\n")),(0,a.kt)("p",null,"The file paths can be either in the local file system or in GCS bucket. When parquet files are provided from GCS bucket,\nDagger will require a ",(0,a.kt)("inlineCode",{parentName:"p"},"core_site.xml")," to be configured in order to connect and read from GCS. A sample ",(0,a.kt)("inlineCode",{parentName:"p"},"core_site.xml")," is\npresent in dagger and looks like this:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-xml"},"<configuration>\n    <property>\n        <name>google.cloud.auth.service.account.enable</name>\n        <value>true</value>\n    </property>\n    <property>\n        <name>google.cloud.auth.service.account.json.keyfile</name>\n        <value>/Users/dummy/secrets/google_service_account.json</value>\n    </property>\n    <property>\n        <name>fs.gs.requester.pays.mode</name>\n        <value>CUSTOM</value>\n        <final>true</final>\n    </property>\n    <property>\n        <name>fs.gs.requester.pays.buckets</name>\n        <value>my_sample_bucket_name</value>\n        <final>true</final>\n    </property>\n    <property>\n        <name>fs.gs.requester.pays.project.id</name>\n        <value>my_billing_project_id</value>\n        <final>true</final>\n    </property>\n</configuration>\n")),(0,a.kt)("p",null,"You can look into the official ",(0,a.kt)("a",{parentName:"p",href:"https://github.com/GoogleCloudDataproc/hadoop-connectors/blob/master/gcs/CONFIGURATION.md"},"GCS Hadoop Connectors")," documentation to know more on how to edit\nthis xml as per your needs."),(0,a.kt)("p",null,"Dagger allows configuring a single data source per stream. The ",(0,a.kt)("a",{parentName:"p",href:"/dagger/docs/reference/configuration#source_details"},"SOURCE_DETAILS"),"\nconfiguration inside STREAMS environment variable needs to be set to the desired source name and source type, along with\nother dependent configs. For the full list of configuration, please check ",(0,a.kt)("a",{parentName:"p",href:"/dagger/docs/reference/configuration"},"here"),"."))}l.isMDXComponent=!0}}]);