# Eclipse sensiNact Rules Whiteboard

The Rules whiteboard is an easy-to-use mechanism for building *reactive* behaviours in the sensiNact gateway. At a high level the Rules whiteboard provides a callback to your code every time that the resource(s) that you're interested in change their value.


## Interacting with the Rules Whiteboard

To register with the Rules Whiteboard you must implement and register an OSGi Service using the `RuleDefinition` interface. It is also recommended that you give your rule a unique name with the `sensinact.rule.name` service property.

### The Input Filter

The input filter of your `RuleDefinition` determines which Providers, Services and Resources your rule is interested in. It is used to collect the initial data snapshot when the `RuleDefinition` is first registered with the whiteboard, and to filter the data events in the gateway to determine when the rule needs to be re-evaluated

### Evaluating the Rule

Evaluation is the process by which the Rules Whiteboard issues a callback to the `RuleDefinition`. The evaluation is passed the latest known snapshot state, and a `ResourceUpdater` that can be used to simply update a value in a resource.

## Limitations of the Rule Whiteboard

Users of the Rules Whiteboard should be aware of the following limitations:

### Avoid Updating Inputs

Evaluation callbacks are permitted to make changes in the gateway, however they should be careful to avoid updating any data which is captured by the [Input Filter](#the-input-filter). If changes are made to input data then this will re-trigger rule evaluation and may result in an infinite loop, consuming gateway resources and potentially disrupting the stability of the whole gateway.

### Not All Updates Trigger Evaluations

Evaluations are triggered based on data notifications, but the Rules Whiteboard will only ever deliver complete snapshots. This means that if a relevant data update occurs then the Rules Whiteboard will obtain a new snapshot, rather than updating a single value. The benefit of this is that the evaluations always represent the true state of the gateway, with no partial updates visible. The drawback is that some update events may be coalesced into a single evaluation callback, therefore the number of evaluation callbacks cannot be assumed to be equal to the number of data updates.

