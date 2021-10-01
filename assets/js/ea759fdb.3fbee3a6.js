"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[7702],{3905:function(e,t,n){n.d(t,{Zo:function(){return p},kt:function(){return g}});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var l=r.createContext({}),c=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},p=function(e){var t=c(e.components);return r.createElement(l.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},m=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,i=e.originalType,l=e.parentName,p=s(e,["components","mdxType","originalType","parentName"]),m=c(n),g=a,d=m["".concat(l,".").concat(g)]||m[g]||u[g]||i;return n?r.createElement(d,o(o({ref:t},p),{},{components:n})):r.createElement(d,o({ref:t},p))}));function g(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var i=n.length,o=new Array(i);o[0]=m;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s.mdxType="string"==typeof e?e:a,o[1]=s;for(var c=2;c<i;c++)o[c]=n[c];return r.createElement.apply(null,o)}return r.createElement.apply(null,n)}m.displayName="MDXCreateElement"},5333:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return s},contentTitle:function(){return l},metadata:function(){return c},toc:function(){return p},default:function(){return m}});var r=n(7462),a=n(3366),i=(n(7294),n(3905)),o=["components"],s={},l="API Monitoring",c={unversionedId:"usecase/api_monitoring",id:"usecase/api_monitoring",isDocsHomePage:!1,title:"API Monitoring",description:"API Monitoring refers to the practice of monitoring APIs in production to gain visibility into performance, availability and functional correctness. You want to get some insights about the metrics in real-time as soon as possible and possibly have some alerting mechanisms for unusual behaviour.",source:"@site/docs/usecase/api_monitoring.md",sourceDirName:"usecase",slug:"/usecase/api_monitoring",permalink:"/dagger/docs/usecase/api_monitoring",editUrl:"https://github.com/odpf/dagger/edit/master/docs/docs/usecase/api_monitoring.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Overview",permalink:"/dagger/docs/usecase/overview"},next:{title:"Feature Ingestion",permalink:"/dagger/docs/usecase/feature_ingestion"}},p=[{value:"How Dagger Solves?",id:"how-dagger-solves",children:[{value:"Sample Schema Definition",id:"sample-schema-definition",children:[]},{value:"Sample Query in Dagger",id:"sample-query-in-dagger",children:[]},{value:"Impact",id:"impact",children:[]}]}],u={toc:p};function m(e){var t=e.components,s=(0,a.Z)(e,o);return(0,i.kt)("wrapper",(0,r.Z)({},u,s,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h1",{id:"api-monitoring"},"API Monitoring"),(0,i.kt)("p",null,"API Monitoring refers to the practice of monitoring APIs in production to gain visibility into performance, availability and functional correctness. You want to get some insights about the metrics in real-time as soon as possible and possibly have some alerting mechanisms for unusual behaviour."),(0,i.kt)("h2",{id:"how-dagger-solves"},"How Dagger Solves?"),(0,i.kt)("p",null,"If you look closely, API health/uptime/performance is mostly a ",(0,i.kt)("em",{parentName:"p"},"real-time streaming aggregation problem")," and hence Dagger can be a tool of choice. The only prerequisite is that you need some sort of structured events to be streamed for each API calls. The events can be streamed by the middleware/API gateway layer with all sort of information."),(0,i.kt)("p",null,"This is sample Data flow of API monitoring in Dagger.\n",(0,i.kt)("img",{alt:"API Monitoring",src:n(3359).Z})),(0,i.kt)("h3",{id:"sample-schema-definition"},"Sample Schema Definition"),(0,i.kt)("p",null,"This is a sample schema of API logs."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-protobuf"},"message SampleAPILog {\n    google.protobuf.Timestamp event_timestamp = 1;\n    int32 http_status_code = 2;\n    string api_method = 13;\n    string api_id = 14;\n    string api_uri = 15;\n    string api_name = 16;\n    string api_upstream_url = 17;\n}\n")),(0,i.kt)("h3",{id:"sample-query-in-dagger"},"Sample Query in Dagger"),(0,i.kt)("p",null,"We want to aggregate APIs on status codes. This will help us bucket APIs which are responding 4XX or 5XX and possibly have some issues."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-SQL"},"SELECT\n  api_name AS api_name,\n  api_uri AS api_uri,\n  api_method AS api_method,\n  Cast(http_status_code AS BIGINT) as http_status_code,\n  count(1) as request_count,\n  Tumble_end(rowtime, INTERVAL '60' second) AS event_timestamp\nFROM\n  `api_logs`\nGROUP BY\n  api_name,\n  api_uri,\n  http_status_code,\n  api_method,\n  Tumble (rowtime, INTERVAL '60' second)\n")),(0,i.kt)("p",null,"This is a Dashboard on a sample API based on the status code powered by Dagger."),(0,i.kt)("p",null,(0,i.kt)("img",{alt:"Profile Enrichment",src:n(3081).Z})),(0,i.kt)("h3",{id:"impact"},"Impact"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"Since Dagger support influx as one of the sinks, user can easily set up alerts and visualise API performance on some visualization tools in near real-time."),(0,i.kt)("li",{parentName:"ul"},"Apart from API uptime all sort of other performance details per API like endpoint latency, proxy latency can be aggregated by Dagger."),(0,i.kt)("li",{parentName:"ul"},"Apart from simple health monitoring you can do a lot of complex streaming analysis on the middleware logs. Since Dagger scales in an instant, the scale of API event logs should not be an issue.")))}m.isMDXComponent=!0},3359:function(e,t,n){t.Z=n.p+"assets/images/api-monitoring-e1b2c92d7cf87a97dedf15f54c16c9e7.png"},3081:function(e,t,n){t.Z=n.p+"assets/images/api-status-08546be3cc26f033417f05a4c5a75f9a.png"}}]);