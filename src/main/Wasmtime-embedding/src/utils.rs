use std::ops::Try;

// Allows you to fold Iter<Try<T>> into Try<Vec<T>>. The residual will be of the first try that fails
pub fn error_accumulator<
    V,
    E,
    A: Try<Output = Vec<V>, Residual = E>,
    I: Try<Output = V, Residual = E>,
>(
    maybe_accumulator: A,
    maybe_value: I,
) -> A {
    let accumulator_branch = maybe_accumulator.branch();

    match accumulator_branch {
        std::ops::ControlFlow::Continue(mut accumulator) => {
            let value_branch = maybe_value.branch();

            match value_branch {
                std::ops::ControlFlow::Continue(value) => {
                    accumulator.push(value);
                    A::from_output(accumulator)
                }
                std::ops::ControlFlow::Break(e) => A::from_residual(e),
            }
        }
        std::ops::ControlFlow::Break(e) => A::from_residual(e),
    }
}
