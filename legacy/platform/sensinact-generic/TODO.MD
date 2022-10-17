# Open Points

## `TestGenericImplementation.testFactory(org.eclipse.sensinact.gateway.generic.test.TestGenericImplementation)
`
According to the original test the props that have been set looked as follows:
```
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
	<entry key="startAtInitializationTime">true</entry>
	<entry key="pir">VALUE</entry>
	<entry key="ldr">VALUE</entry>
	<entry key="gpr">VALUE</entry>
</properties>
```

If we do this, the test is not working. If I rename the ldr value it works like a charm. Can't see how this originally worked.

## `TestGenericImplementation.testExtraCatalogs`

No clue how this should work. The extra-5 and extra-6 bundles are Fragments and should have the resource xmls at the same place as the original tests. It isn't working though and looking at the `org.eclipse.sensinact.gateway.generic.test.tb.moke4.Activator`gives me no clue how this could work anyway. The activator is not looking for any other resource xmls then `genova-resource_0.xml`. Not sure how this should work.